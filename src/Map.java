import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.imageio.ImageIO;

public class Map {
    public static final int WIDTH = 16;
    public static final int HEIGHT = 12;
    public static final int TILE_SIZE = 64;

    private BufferedImage tileGrass, tilePath, tileSpawn, tileGoal;
    private final java.util.List<Point> path = new ArrayList<>();

    public Map(){
        loadTiles();
        buildPath();
    }

    private void loadTiles(){
        try{
            tileGrass = ImageIO.read(new File("/home/jordans/TowerDefense/Assets/Tiles/tile_grass.png"));
            tilePath  = ImageIO.read(new File("/home/jordans/TowerDefense/Assets/Tiles/tile_path.png"));
            tileSpawn = ImageIO.read(new File("/home/jordans/TowerDefense/Assets/Tiles/tile_spawn.png"));
            tileGoal  = ImageIO.read(new File("/home/jordans/TowerDefense/Assets/Tiles/tile_goal.png"));
        } catch(IOException e){ e.printStackTrace(); }
    }

    private void buildPath(){
        path.add(new Point(0,5));
        path.add(new Point(1,5));
        path.add(new Point(2,5));
        path.add(new Point(2,6));
        path.add(new Point(2,7));
        path.add(new Point(3,7));
        path.add(new Point(4,7));
        path.add(new Point(5,7));
        path.add(new Point(5,6));
        path.add(new Point(5,5));
        path.add(new Point(6,5));
        path.add(new Point(7,5));
        path.add(new Point(7,4));
        path.add(new Point(7,3));
        path.add(new Point(8,3));
        path.add(new Point(9,3));
        path.add(new Point(9,4));
        path.add(new Point(9,5));
        path.add(new Point(10,5));
        path.add(new Point(11,5));
        path.add(new Point(12,5));
        path.add(new Point(12,6));
        path.add(new Point(12,7));
        path.add(new Point(13,7));
        path.add(new Point(14,7));
        path.add(new Point(15,7));
    }

    public void draw(Graphics g){
        for(int y=0; y<HEIGHT; y++){
            for(int x=0; x<WIDTH; x++){
                BufferedImage tile = tileGrass;
                for(Point p : path){
                    if(p.x==x && p.y==y){ tile = tilePath; break; }
                }
                if(x==path.get(0).x && y==path.get(0).y) tile=tileSpawn;
                if(x==path.get(path.size()-1).x && y==path.get(path.size()-1).y) tile=tileGoal;
                g.drawImage(tile, x*TILE_SIZE, y*TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
            }
        }
    }

    public java.util.List<Point> getPath(){ return path; }
}
