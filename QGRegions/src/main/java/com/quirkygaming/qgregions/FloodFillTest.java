package com.quirkygaming.qgregions;

import java.util.LinkedList;
import java.util.Queue;
import java.util.TreeSet;

import com.quirkygaming.commons.coordinate.Coord2D;

public class FloodFillTest {
	
	@SuppressWarnings("serial")
	public static void main(String[] args) {
		TreeSet<Coord2D> bounds = new TreeSet<Coord2D>(){{
			add(new Coord2D(13,0));
			add(new Coord2D(14,0));
			add(new Coord2D(18,0));
			add(new Coord2D(13,1));
			add(new Coord2D(15,1));
			add(new Coord2D(17,1));
			add(new Coord2D(18,1));
			add(new Coord2D(13,2));
			add(new Coord2D(16,2));
			add(new Coord2D(18,2));
			add(new Coord2D(13,3));
			add(new Coord2D(14,3));
			add(new Coord2D(18,3));
			add(new Coord2D(13,4));
			add(new Coord2D(17,4));
			add(new Coord2D(12,5));
			add(new Coord2D(17,5));
			add(new Coord2D(13,6));
			add(new Coord2D(16,6));
			add(new Coord2D(13,7));
			add(new Coord2D(14,7));
			add(new Coord2D(15,7));
		}};
		TreeSet<Coord2D> fill = new TreeSet<Coord2D>();
		
		Coord2D origin = new Coord2D(15,5);
		
		print(bounds);
		
		System.out.println(floodFill(origin, bounds, fill));
		
		print(fill);
	}
	
	public static boolean floodFill(Coord2D node, TreeSet<Coord2D> bounds, TreeSet<Coord2D> fill) {
		Queue<Coord2D> q = new LinkedList<Coord2D>();
		if (bounds.contains(node)) return false; // On top of boundary
		q.add(node);
		fill.addAll(bounds);
		while (!q.isEmpty()) {
			Coord2D w = q.remove();
			Coord2D e = w;
			while (!fill.contains(w.west())) {
				w = w.west();
				if (e.x - w.x > bounds.size()) return false; // Outside of boundaries
			}
			while (!fill.contains(e)) {
				e = e.east();
				if (e.x - w.x > bounds.size()) return false; // Outside of boundaries
			}
			while (!w.equals(e)) {
				fill.add(w);
				if (!fill.contains(w.north())) q.add(w.north());
				if (!fill.contains(w.south())) q.add(w.south());
				w = w.east();
				print(fill);
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			
		}
		
		return true;
	}
	
	public static void print(TreeSet<Coord2D> entries) {
		for (int y = -2; y < 10; y++) {
			for (int x = -2; x < 25; x++) {
				System.out.print((entries.contains(new Coord2D(x,y))) ? "█" : " ");
			}
			System.out.println();
		}
	}
}
