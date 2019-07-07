package com.quirkygaming.qgbooks;

import static com.quirkygaming.commons.Util.plural;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.java.JavaPlugin;

import com.quirkygaming.commons.CommonsAPI;
import com.quirkygaming.commons.GreeterRegister.GreeterPriority;
import com.quirkygaming.commons.listeners.GreeterListener;
import com.quirkygaming.commons.playerinterface.PlayerInfo;
import com.quirkygaming.commons.playerinterface.ServerCommandEventCanceller;
import com.quirkygaming.qgbooks.config.Database;
import com.quirkygaming.qgbooks.entries.Bulletin;
import com.quirkygaming.qgbooks.entries.CompatableEntry;
import com.quirkygaming.qgbooks.entries.GenericEntry;
import com.quirkygaming.qgbooks.entries.PrivateMessage;
import com.quirkygaming.qgbooks.entries.SelfNote;
import com.quirkygaming.qgbooks.entries.TimeData;

public class QGBooks extends JavaPlugin implements GreeterListener {
	
	public static QGBooks INSTANCE;
	
	ShapelessRecipe messageBookRecipe;
	
	HashMap<String, Long> lastPlayed = new HashMap<String, Long>();
	
	private static final String titlePage;
	private static final String lineFeed = "\n";
	
	static {
		titlePage = "§1-%NAME's Message Book-§0" + lineFeed
				+ lineFeed
				+ "%NAME has a total of §l§o%MESSAGES§r messages." + lineFeed
				+ lineFeed
				+ "Use the '/qgbooks help' command for instructions."
				;
	}
	
	@Override
	public void onEnable() {
		INSTANCE = this;
		addCraftingRecipes();
		
		Database.loadConfig(this);
		
		setInitialLastPlayed();
		
		new BooksListener(this);
		new QGCommandExecutor(this);
		
		CommonsAPI.subscribeToGreetingMessages(this, GreeterPriority.BOTTOM);
		
	}
	
	@Override
	public void onDisable() {
		
	}

	@Override
	public void sendGreeting(Player p) {
		sendUnreadInfo(p);
	}
	
	void setInitialLastPlayed() {
		for (OfflinePlayer p : getServer().getOfflinePlayers()) {
			lastPlayed.put(p.getName(), p.getLastPlayed());
		}
	}
	
	void sendUnreadInfo(Player p) {
		//Check for new messages
		if (lastPlayed.containsKey(p.getName())) {
			int bulletins = 0;
			int pms = 0;
			
			long p_lastPlayed = lastPlayed.get(p.getName());
			
			for (long ID : Database.getPlayerPageIDs(p.getUniqueId())) {
				CompatableEntry b = Database.getPageByID(ID);
				long entryTime = b.epochFormat();
				if (entryTime > p_lastPlayed) {
					if (b.isBulletin()) {
						bulletins++;
					} else {
						pms++;
					}
				}
			}
			
			int total = bulletins + pms;
			
			if (total > 0) {
				if (bulletins > 0) {
					p.sendMessage(ChatColor.BLUE + "You have " + ChatColor.RED + bulletins + ChatColor.BLUE + " unread " + plural(bulletins, "bulletin", "bulletins") + ".");
				}
				if (pms > 0) {
					p.sendMessage(ChatColor.BLUE + "You have " + ChatColor.RED + pms + ChatColor.BLUE + " unread private " + plural(pms, "message", "messages") + ".");
				}
				p.sendMessage(ChatColor.BOLD + "" + ChatColor.BLUE + "Check your message book to read " + plural(total, "it", "them") + ".");
			}
		}
		lastPlayed.put(p.getName(), new Date().getTime());
	}
	
	public ItemStack setupBookItem(ItemStack writtenBook, Player player) {
		BookMeta book = (BookMeta) writtenBook.getItemMeta();
		
		book.setAuthor("SERVER");
		book.setTitle("Message Book");

		writtenBook.setItemMeta(book);
		
		updateBook(player, writtenBook, book);
		
		return writtenBook;
	}
	
	void handleCraftingEvent(CraftItemEvent event) {
		if (event.getRecipe().getResult().getType() == Material.WRITTEN_BOOK && 
				event.getWhoClicked() instanceof Player) {
			
			ItemStack craftedBook = event.getCurrentItem();
			setupBookItem(craftedBook , (Player) (event.getWhoClicked()));
		}
	}
	
	boolean processEvent(Player p, ItemStack item) {
		if (item.getAmount() == 1 && item.getType() == Material.WRITTEN_BOOK) {
			
			BookMeta book = (BookMeta) item.getItemMeta();
			
			if (book.getAuthor() != null && book.getAuthor().equals("QuirkyGaming")) {
				book.setAuthor("SERVER");
			}
			if (book.getAuthor() != null && book.getAuthor().equals("SERVER")) {
				updateBook(p, item, book);
				return true;
			}
		}
		return false;
	}
	
	boolean processCommandEvent(ServerCommandEvent event) {
		String message = event.getCommand();
		return processMessageCmd(message, event.getSender(), new ServerCommandEventCanceller(event));
	}
	
	boolean processChatEvent(AsyncPlayerChatEvent event) {
		String message = event.getMessage();
		return processMessageCmd(message, event.getPlayer(), event);
	}
	
	public boolean sendMessageViaString(String message, CommandSender sender) {
		return processMessageCmd(message, sender, null);
	}
	
	boolean processMessageCmd(String message, CommandSender player, Cancellable event) {
		
		boolean useBook = message.startsWith("@");
		
		if ( (message.startsWith("#") || useBook) && message.indexOf(" ") > 1) {
			
			String senderName = player.getName();
			
			int dividerLocation = message.indexOf(" ");
			String rawRecipientName = message.substring(1, dividerLocation);
			
			String recipientName;
			UUID recipientID;
			
			if (rawRecipientName.equalsIgnoreCase("console") || rawRecipientName.equalsIgnoreCase("server")) {
				rawRecipientName = "CONSOLE"; recipientName = "CONSOLE"; recipientID = Database.CONSOLE;
				//useBook = false;
			} else {
				PlayerInfo recipientInfo = new PlayerInfo(rawRecipientName);
				recipientName = recipientInfo.username();
				recipientID = recipientInfo.uniqueID();
			}
			
			String messageHead = ChatColor.GOLD + "{From " + senderName + ", to " + recipientName + "} ";
			String bulletinHead = ChatColor.GOLD + "{Bulletin from " + senderName + "} ";
			String messageBody = message.substring(dividerLocation + 1);
			
			if (messageBody == null) {
				// A message with the length of 0 (ie. "@Vallamont ") will create a null message
				return false;
			}
			
			if (event != null) event.setCancelled(true);
			
			if (rawRecipientName.equalsIgnoreCase("all")) {
				String messages[] = new String[]{bulletinHead, messageBody};
				
				for (Player p : getServer().getOnlinePlayers()) {
					p.sendMessage(messages);
				}
				Bulletin b = new Bulletin(Database.nextID(), messageBody, Database.senderID(player), TimeData.newCurrentTime());
				if (useBook) addBookEntry(b);
					//addBookEntries(messageBody, senderName, rawRecipientName, true);
				return true;
				
			} else {
				String messages[] = new String[]{messageHead, messageBody};
				
				boolean offline = false;
				boolean playerExists = true;
				CommandSender recipient_msg = Bukkit.getConsoleSender();
				
				if (!rawRecipientName.equals("CONSOLE")) {
					PlayerInfo playerInfo = new PlayerInfo(recipientName);
					offline = playerInfo.offline();
					playerExists = (playerInfo.lastLogin() != null);
					if (!offline) recipient_msg = playerInfo.player();
				}
				
				boolean selfNote = senderName.equals(recipientName);
				
				if (!offline) {
					player.sendMessage(messages);
					if (!selfNote) {
						
						recipient_msg.sendMessage(messages);
					}
					player.sendMessage(ChatColor.GOLD + "Message was sent successfully.");
				} else {
					if (!useBook) {
						player.sendMessage(ChatColor.RED + recipientName + " was not found online.");
					}
				}
				if (playerExists) {
					if (useBook) {
						GenericEntry m;
						if (selfNote) {
							m = new SelfNote(
									Database.nextID(),
									messageBody,
									Database.senderID(player),
									TimeData.newCurrentTime());
						} else {
							m = new PrivateMessage(
									Database.nextID(), 
									messageBody, 
									Database.senderID(player), 
									TimeData.newCurrentTime(), 
									recipientID);
						}
						addBookEntry(m);
						
						if (offline && !selfNote) player.sendMessage(messages);
						player.sendMessage(ChatColor.GOLD + "Message was sent to " + recipientName + "'s message book.");
						return true;
					}
					return false;
				} else {
					player.sendMessage(ChatColor.RED + "There is no player by the name of " + recipientName + " on this server.");
					return false;
				}
			}
		}
		
		
		return false;
	}
	
	private void updateBook(Player p, ItemStack bookStack, BookMeta bookMeta) {
		
		//TODO bookMeta.setTitle("Message Book");
		
		ArrayList<String> pagesArray = new ArrayList<String>();
		
		for (long ID : Database.getPlayerPageIDs(p.getUniqueId())) {
			CompatableEntry currentEntry = Database.getPageByID(ID);
			pagesArray.add(0,currentEntry.getPage(p.getUniqueId()));
		}
		
		//Title page
		pagesArray.add(0, 
				titlePage
				.replaceAll("%NAME", p.getName())
				.replaceAll("%MESSAGES", Integer.toString(pagesArray.size()))
				);
		
		String[] pageArray = new String[pagesArray.size()];
		pageArray = pagesArray.toArray(pageArray);
		bookMeta.setPages(pageArray);
		
		bookStack.setItemMeta(bookMeta);
	}
	
	void addBookEntry(GenericEntry e) {
		Database.addEntry(e);
	}
	
	void addCraftingRecipes() {
		
		ItemStack messageBookStack = new ItemStack(Material.WRITTEN_BOOK,1);
		BookMeta bookMeta = (BookMeta) messageBookStack.getItemMeta();
		bookMeta.setTitle("Message Book");
		messageBookStack.setItemMeta(bookMeta);
		messageBookRecipe = new ShapelessRecipe(messageBookStack);
		messageBookRecipe.addIngredient(Material.REDSTONE).addIngredient(Material.BOOK);
		
		getServer().addRecipe(messageBookRecipe);
	}
	
}
