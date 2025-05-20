// Game.java
package program;

import GUI.MainWindow;

public class Game {
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(() -> {
			MainWindow window = new MainWindow();
			window.setVisible(true);
		});
	}
}