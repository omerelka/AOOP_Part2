package GUI;

import java.awt.*;

public class TruckVisual {
    private Point origin;
    private Point destination;
    private double progress; // from 0.0 to 1.0
    private Color color = Color.BLACK;
    private boolean visible = true;

    public TruckVisual(Point origin, Point destination) {
        this.origin = origin;
        this.destination = destination;
        this.progress = 0.0;
    }

    public void setProgress(double progress) {
        this.progress = Math.max(0.0, Math.min(1.0, progress));
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setOrigin(Point origin) {
        this.origin = origin;
    }

    public void setDestination(Point destination) {
        this.destination = destination;
    }

    public void draw(Graphics2D g2) {
        if (!visible || origin == null || destination == null) return;

        int x = (int) (origin.x + (destination.x - origin.x) * progress);
        int y = (int) (origin.y + (destination.y - origin.y) * progress);

        g2.setColor(color);
        g2.fillRect(x, y, 16, 16); // truck body

        g2.setColor(Color.BLACK); // wheels
        g2.fillOval(x - 5, y - 5, 10, 10);     // top-left
        g2.fillOval(x + 11, y - 5, 10, 10);    // top-right
        g2.fillOval(x - 5, y + 11, 10, 10);    // bottom-left
        g2.fillOval(x + 11, y + 11, 10, 10);   // bottom-right
    }

    public double getProgress() {
        return progress;
    }

    public Point getOrigin() {
        return origin;
    }

    public Point getDestination() {
        return destination;
    }
    public void autoSetColor(String truckType, boolean isLoaded, boolean isToSender) {
        switch (truckType) {
            case "VAN":
                color = Color.BLUE.darker(); // Dark blue
                break;
            case "STANDARD":
                color = isLoaded ? Color.GREEN : Color.green.darker(); // Dark green if loaded
                break;
            case "NONSTANDARD":
                color = isToSender ? Color.RED.brighter() : Color.red; // Bright red to sender, dark red to destination
                break;
            default:
                color = Color.BLACK;
        }
    }
}
