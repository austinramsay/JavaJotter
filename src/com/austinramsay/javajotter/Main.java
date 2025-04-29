package com.austinramsay.javajotter;

import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JavaJotter jj = new JavaJotter();
            }
        });
    }
}