package com.taurus.permanent;

public class Main {
	
	public static void main(String[] args) {
		TPServer taurus = TPServer.me();
		taurus.start();
	}
}
