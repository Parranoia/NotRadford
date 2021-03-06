package game.entity;

import game.Game;
import game.entity.projectile.Projectile;
import game.graphics.SpriteSheet;
import game.handlers.DamageEvent;
import game.pathfinding.Path;

import java.awt.*;
import java.util.ArrayList;

public abstract class Entity {

    public int WIDTH;
    public int HEIGHT;
    protected int MAX_X;
    protected int MAX_Y;

    protected Point position;
    protected int health;
    protected int maxHealth;
    protected int speed;
    protected Rectangle bounds;
    protected Game game;
    protected ArrayList<DamageEvent> damageEvents;

    protected Direction facing;
    protected boolean moving;
    protected SpriteSheet sprites;
    protected int anim;
    protected Image sprite;

    private Point lastPoint;
    private int failedAttempts;

    public Entity(int x, int y, Game game) {
        position = new Point(x, y);
        this.game = game;
        damageEvents = new ArrayList<DamageEvent>();
        anim = 0;
        facing = Direction.DOWN;
        moving = false;

        lastPoint = null;
        failedAttempts = 0;
    }

    /**
     * Must be called at the end of the child constructor to initialize the
     * MAX_X and MAX_Y variables
     */
    protected void init() {
        MAX_X = (Game.WIDTH * Game.SCALE) - WIDTH + 10;
        MAX_Y = (Game.HEIGHT * Game.SCALE) - HEIGHT + 10;

        health = maxHealth;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getX() {
        return position.x;
    }

    public int getY() {
        return position.y;
    }

    public Point getPosition() {
        return position;
    }

    public Point getCenterPosition() {
        return new Point(position.x + (WIDTH / 2), position.y + (HEIGHT / 2));
    }

    /**
     * @return The center X coordinate of this entity
     */
    public int getCenterX() {
        return getCenterPosition().x;
    }

    /**
     * @return The center Y coordinate of this entity
     */
    public int getCenterY() {
        return getCenterPosition().y;
    }

    /**
     * Moves the entity in the specified Direction
     *
     * @param d                The Direction in which to move
     * @param multipleMovement Whether or not this entity will be moving in more than one direction (Diagonal)
     */
    public boolean move(Direction d, boolean multipleMovement) {
        int mod = multipleMovement ? 2 : 1;

        facing = d;
        moving = true;

        switch (d) {
            case UP:
                if (Game.bounds.contains(getX(), getY() - (speed / mod), WIDTH, HEIGHT)
                        && game.getLevel().isTraversable(new Rectangle(getX(), getY() - (speed / mod), WIDTH, HEIGHT))) {
                    position.y -= (speed / mod);
                    return true;
                }
                break;
            case DOWN:
                if (Game.bounds.contains(getX(), getY() + (speed / mod), WIDTH, HEIGHT)
                        && game.getLevel().isTraversable(new Rectangle(getX(), getY() + (speed / mod), WIDTH, HEIGHT))) {
                    position.y += (speed / mod);
                    return true;
                }
                break;
            case RIGHT:
                if (Game.bounds.contains(getX() + (speed / mod), getY(), WIDTH, HEIGHT)
                        && game.getLevel().isTraversable(new Rectangle(getX() + (speed / mod), getY(), WIDTH, HEIGHT))) {
                    position.x += (speed / mod);
                    return true;
                }
                break;
            case LEFT:
                if (Game.bounds.contains(getX() - (speed / mod), getY(), WIDTH, HEIGHT)
                        && game.getLevel().isTraversable(new Rectangle(getX() - (speed / mod), getY(), WIDTH, HEIGHT))) {
                    position.x -= speed / mod;
                    return true;
                }
                break;
        }
        return false;
    }

    /**
     * Moves the entity along the path specified
     * @param path The path the entity is to follow
     */
    public void traversePath(Path path) {
        Point p = lastPoint;
        if (lastPoint == null) {
            p = path.getNext();
            p = new Point(p.x * 8, p.y * 8);
            lastPoint = p;
        }

        int directions = 0;

        if (getX() < p.x) directions++;
        if (getX() > p.x) directions++;
        if (getY() < p.y) directions++;
        if (getY() > p.y) directions++;

        if (getX() < p.x && getX() + speed < MAX_X)
            if (!move(Direction.RIGHT, directions > 1)) failedAttempts++;
        if (getX() > p.x && getX() - speed >= 0)
            if (!move(Direction.LEFT, directions > 1)) failedAttempts++;
        if (getY() < p.y && getY() + speed < MAX_Y)
            if (!move(Direction.DOWN, directions > 1)) failedAttempts++;
        if (getY() > p.y && getY() - speed >= 0)
            if (!move(Direction.UP, directions > 1)) failedAttempts++;

        if (distanceTo(lastPoint) < 40)
            lastPoint = null;

        if (failedAttempts >= 3) {
            path.clear();
            failedAttempts = 0;
            lastPoint = null;
        }
    }

    /**
     * Returns the euclidean distance between two entities
     * sqrt((x1 - x2)^2 + (y1 - y2)^2)
     *
     * @param e The entity to get the distance between
     * @return Returns the euclidean distance between this entity and the Entity passed in
     */
    public double distanceTo(Entity e) {
        return Math.sqrt(Math.pow(e.getCenterX() - getCenterX(), 2.0) + Math.pow(e.getCenterY() - getCenterY(), 2.0));
    }

    /**
     * Returns the euclidean distance between two entities
     * sqrt((x1 - x2)^2 + (y1 - y2)^2)
     *
     * @param p The point to get the distance between
     * @return Returns the euclidean distance between this entity and the Entity passed in
     */
    public double distanceTo(Point p) {
        return Math.sqrt(Math.pow(p.x - getCenterX(), 2) + Math.pow(p.y - getCenterY(), 2));
    }

    /**
     * Adds or removes health from the entity
     * Positive values add, negative subtract
     *
     * @param amount The amout of health to add/remove
     */
    public void addHealth(int amount) {
        if (amount < 0)
            damageEvents.add(new DamageEvent(this, Math.abs(amount)));
        health += amount;
        if (health < 0)
            health = 0;
    }

    /**
     * @return The amount of health this entity currently has
     */
    public int getHealth() {
        return health;
    }

    public void setMaxHealth(int health) {
        maxHealth = health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public void restoreHealth() {
        health = maxHealth;
    }

    /**
     * This method must be overwritten and then called in child classes.
     * It keeps track of the bounds of the entity as well as updating any
     * DamageEvents that may have occurred. It also handles animating sprites
     * of this Entity. The child class has to set moving = false in it's
     * implementation to ensure the animations work correctly
     *
     * @param diff The time difference in milliseconds since the last update.
     */
    public void update(double diff) {
        bounds = new Rectangle(position, new Dimension(WIDTH, HEIGHT));

        for (DamageEvent de : damageEvents)
            de.tick();
        for (int i = 0; i < damageEvents.size(); i++)
            if (damageEvents.get(i).getTicks() < 0)
                damageEvents.remove(i);

        anim = anim < 10000 ? ++anim : 0;
        if (sprite == null)
            return;
        if (moving) {
            switch (facing) {
                case UP:
                    if (anim % 20 < 10)
                        sprite = sprites.getSprite(13);
                    else
                        sprite = sprites.getSprite(15);
                    break;
                case DOWN:
                    if (anim % 20 < 10)
                        sprite = sprites.getSprite(1);
                    else
                        sprite = sprites.getSprite(3);
                    break;
                case LEFT:
                    if (anim % 10 == 0)
                        sprite = sprites.getSprite(5);
                    if (anim % 20 == 0)
                        sprite = sprites.getSprite(6);
                    if (anim % 30 == 0)
                        sprite = sprites.getSprite(7);
                    break;
                case RIGHT:
                    if (anim % 10 == 0)
                        sprite = sprites.getSprite(9);
                    if (anim % 20 == 0)
                        sprite = sprites.getSprite(10);
                    if (anim % 30 == 0)
                        sprite = sprites.getSprite(11);
                    break;
            }
        }
        else {
            switch (facing) {
                case UP:
                    sprite = sprites.getSprite(12);
                    break;
                case DOWN:
                    sprite = sprites.getSprite(0);
                    break;
                case LEFT:
                    sprite = sprites.getSprite(4);
                    break;
                case RIGHT:
                    sprite = sprites.getSprite(8);
                    break;
            }
        }
    }

    /**
     * This method must be overwritten and then called in child classes.
     * It handles rendering any DamageEvents available as well as rendering
     * the sprite that represents this entity
     *
     * @param g The Graphics object to draw on
     */
    public void render(Graphics2D g) {
        g.drawImage(sprite, getX(), getY(), WIDTH, HEIGHT, null);

        for (DamageEvent de : damageEvents)
            de.render(g);
    }

    /**
     * @return Returns true if this entity is a player, false otherwise
     */
    public boolean isPlayer() {
        return getClass().equals(Player.class);
    }

    /**
     * @return Returns true if this entity is a projectile, false otherwise
     */
    public boolean isProjectile() {
        return this instanceof Projectile;
    }

    /**
     * @return Returns true if this entity is an enemy, false otherwise
     */
    public boolean isEnemy() {
        return getClass().equals(Enemy.class);
    }

    public void setSprites(SpriteSheet sprites) {
        this.sprites = sprites;
    }

    public void setSprite(Image img) {
        sprite = img;
        WIDTH = (int)(sprite.getWidth(null) * 1.5);
        HEIGHT = (int)(sprite.getHeight(null) * 1.5);
    }

}
