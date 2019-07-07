package com.quirkygaming.qgheads;

import org.apache.commons.codec.binary.Base64;

public class Test {
	public static void main(String[] args) {
		System.out.println(new String(Base64.decodeBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDE2NDVkZmQ3N2QwOTkyMzEwN2IzNDk2ZTk0ZWViNWMzMDMyOWY5N2VmYzk2ZWQ3NmUyMjZlOTgyMjQifX19")));
		System.out.println(new String(Base64.decodeBase64("eyJ0aW1lc3RhbXAiOjE0MzMyNzg3MTY1MDYsInByb2ZpbGVJZCI6IjIzY2JlNjZmNWU0ZjRhOGZiZDAwMDQ0NDA1OWM5YjU3IiwicHJvZmlsZU5hbWUiOiIxMTAwMTAwIiwiaXNQdWJsaWMiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsIjoiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9kNGE4ZWRmM2QxOTc0MmRlOWFjZjQwOGZmNGQ2MTcyNjE2MWMzNmQ0NTNmNzJlZjcyOWJkZDk5NDNkNmFmIn19fQ==")));
		System.out.println(new String(Base64.decodeBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDRhOGVkZjNkMTk3NDJkZTlhY2Y0MDhmZjRkNjE3MjYxNjFjMzZkNDUzZjcyZWY3MjliZGQ5OTQzZDZhZiJ9fX0=")));
		System.out.println(Base64.encodeBase64String("{\"textures\":{\"SKIN\":{\"url\":\"http://textures.minecraft.net/texture/d4a8edf3d19742de9acf408ff4d61726161c36d453f72ef729bdd9943d6af\"}}}".getBytes()));
	}
}
