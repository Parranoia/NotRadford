package game.entity;

import game.Game;
import game.entity.projectile.CircleProjectile;
import game.handlers.InputHandler;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Player extends Entity {

    private InputHandler input;
    private BufferedImage healthIcon, greyHealthIcon;
    private double shootTimer;

    public Player(int x, int y, Game game) {
        super(x, y, game);
        WIDTH = 56;
        HEIGHT = 56;
        bounds = new Rectangle(position, new Dimension(WIDTH, HEIGHT));
        speed = 4;
        maxHealth = 3;

        shootTimer = 500;

        init();

        try {
            healthIcon = ImageIO.read(new File(getClass().getResource("/res/images/heart.png").getFile()));
            greyHealthIcon = ImageIO.read(new File(getClass().getResource("/res/images/greyHeart.png").getFile()));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void setInput(InputHandler input) {
        this.input = input;
    }

    public void sendProjectile(Direction d) {
        game.addEntity(new CircleProjectile(getCenterX(), getCenterY(), game, this, d, 7, 3, 8));
    }

    public void update(double diff) {
        super.update(diff);

        if (shootTimer <= diff) {
            if (input.keys.containsKey(KeyEvent.VK_UP))
                sendProjectile(Direction.UP);
            else if (input.keys.containsKey(KeyEvent.VK_DOWN))
                sendProjectile(Direction.DOWN);
            else if (input.keys.containsKey(KeyEvent.VK_LEFT))
                sendProjectile(Direction.LEFT);
            else if (input.keys.containsKey(KeyEvent.VK_RIGHT))
                sendProjectile(Direction.RIGHT);
            shootTimer = 500;

        } else shootTimer -= diff;

        int[] keys = { KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_A, KeyEvent.VK_D };

        int latestKey = 0;
        long mostRecent = 0;

        for (int i = 0; i < keys.length; i++)
            if (input.keys.containsKey(keys[i]))
                if (mostRecent < input.keys.get(keys[i])) {
                    mostRecent = input.keys.get(keys[i]);
                    latestKey = keys[i];
                }

        switch (latestKey) {
            case KeyEvent.VK_W:
                move(Direction.UP, false);
                break;
            case KeyEvent.VK_S:
                move(Direction.DOWN, false);
                break;
            case KeyEvent.VK_D:
                move(Direction.RIGHT, false);
                break;
            case KeyEvent.VK_A:
                move(Direction.LEFT, false);
                break;
        }

        if (health == 0)
            game.reset();
    }

    public void render(Graphics2D g) {
        super.render(g);

        g.setColor(Color.BLACK);
        g.fillRect(getX(), getY(), WIDTH, HEIGHT);

        for (int i = 0; i < maxHealth; i++)
            g.drawImage(greyHealthIcon, i * 20 + 10, 10, greyHealthIcon.getWidth(null), greyHealthIcon.getHeight(null), null);

        for (int i = 0; i < health; i++)
            g.drawImage(healthIcon, i * 20 + 10, 10, healthIcon.getWidth(null), healthIcon.getHeight(null), null);

    }

}
