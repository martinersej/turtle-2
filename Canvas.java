/**
 * Import JavaX
 */
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.imageio.ImageIO;
import javax.swing.JButton;

/**
 * Import Java AWT
 */
import java.awt.image.BufferedImage;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.AlphaComposite;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;

/**
 * Import Java IO
 */
import java.io.File;
import java.io.IOException;

/**
 * Import Java util
 */
import java.util.Set;
import java.util.HashSet;

/**
 * Graphical User Interface for the Turtle-assignment (dIntProg)
 * 
 * @author Nikolaj Ignatieff Schwartzbach.
 * @version 2020-08-12.
 */
public class Canvas 
{
    //---------------------\\
    //---FIELD VARIABLES---\\
    //---------------------\\
    
    /** The Canvas. */
    public static Canvas canvas;
    
    static {
        try{
            canvas = new Canvas(600, 600);   
        } catch(IOException e){
            System.out.println("Could not create canvas.");
        }
    }
    
    /** Width and height of the Canvas object (in pixels). */
    private int width, height;
    
    /** The JPanel where turtles are drawn. */
    private ActorPanel panel;
    
    /** The JPanel which contains the ActorPanel and buttons. */
    private JPanel main;
    
    /** The JFrame, the main GUI. */
    private JFrame frame;
    
    /** Current state of the grid (starts at grid=100). */
    private int gridState = 1;
    
    //---------------------\\
    //---  CONSTRUCTOR  ---\\
    //---------------------\\
    /**
     * Instantiates a new Canvas object.
     * @param   width   The width in pixels of this Canvas.
     * @param   height  The height in pixels of this Canvas.
     */
    private Canvas(int width, int height) throws IOException {
        //Set the width of this Canvas
        this.width  = width;
        this.height = height;
        
        //Initialize container JPanel
        main = new JPanel(new GridBagLayout());
        
        //Initialize ActorPanel
        panel = new ActorPanel(width, height);
        panel.setPreferredSize(new Dimension(width, height));
        panel.drawGrid(100);
        
        //GridBagConstraints for layout
        GridBagConstraints c = new GridBagConstraints();
        
        //Set constraints for ActorPanel
        c.fill      = GridBagConstraints.HORIZONTAL;
        c.gridx     = 0;
        c.gridy     = 0;
        c.gridwidth = 2;
        c.weightx   = 0;
        c.weighty   = 0;
        
        //Add ActorPanel to container JPanel
        main.add(panel, c);
      
        //Initialize JButtons
        JButton btnGrid  = new JButton("Grid size");
        JButton btnClear = new JButton("Clear canvas");
        
        //Add Toggle grid function
        btnGrid.addActionListener( e -> {   panel.clearGrid();
                                            if(gridState==0){
                                                panel.drawGrid(100);
                                            }
                                            else if(gridState==1){
                                                panel.drawGrid(25);
                                            }
                                            gridState = (gridState+1)%3; } );
        
        //Add Clear Canvas function
        btnClear.addActionListener( e -> { panel.clear(); } );
        
        //Set constraints for JButtons and add them
        c.gridwidth = 1;
        c.weightx   = 0.5;
        c.gridx     = 0;
        c.gridy     = 1;
        main.add(btnGrid, c);
        c.gridx     = 1;
        main.add(btnClear, c);
        
        //Initialize and setup the the JFrame
        frame = new JFrame("Turtle Graphics");
        frame.add(main);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setContentPane(main);
        frame.setVisible(true);
        frame.pack();
    }
        
    //------------------------\\
    //--- ACCESSOR METHODS ---\\
    //------------------------\\
    
    /** 
     * Get the current Canvas object.
     * @return The current Canvas object.
     */
    public static final Canvas getCanvas() {
        return canvas;
    }
    
    
    /**
     * Returns the width (in pixels) of this Canvas.
     * @return  The width of this Canvas in pixels.
     */
    public int getWidth() {
        return width;
    }
    
    /**
     * Returns the height (in pixels) of this Canvas.
     * @return  The height of this Canvas in pixels.
     */
    public int getHeight() {
        return height;
    }
        
    //-----------------------\\
    //--- MUTATOR METHODS ---\\
    //-----------------------\\

    /**
     * Adds the specified Actor to this Canvas.
     * @param   a   Reference to an Actor object.
     */
    public void addActor(Actor a) {
        panel.addActor(a);
    }
    
    /**
     * Draws a line on the canvas of this Canvas object.
     * @param   x1  First x-coordinate.
     * @param   y1  First y-coordinate.
     * @param   x2  Second x-coordinate.
     * @param   y2  Second y-coordinate.
     * @param   col The Color of the line.
     */
    public void drawLine(double x1, double y1, double x2, double y2, Color col) {
        panel.drawLine((int)x1, (int)y1, (int)x2, (int)y2, col);
    }
    
    /**
     * Updates the content pane of this Canvas object.
     */
    public void update() {
        frame.repaint();
    }
    
    /**
     * Clears the content pane of this Canvas object (will remove all drawn lines).
     */
    public void clear() {
        panel.clear();
    }
    
    /**
     * Draws a grid of a specified size on this Canvas object.
     * @param   i   The spacing between grid cells (in pixels).
     */
    public void drawGrid(int i){
        panel.drawGrid(i);
    }
    
    /**
     * Clears the grid on this Canvas object.
     */
    public void clearGrid(){
       panel.clearGrid(); 
    }
    
}

/**
 * ActorPanel class.
 * Extends the JPanel component to make an AWT component that draws Actors.
 * 
 * @author (Nikolaj Ignatieff Schwartzbach).
 */
class ActorPanel extends JPanel {
    
    //---------------------\\
    //---FIELD VARIABLES---\\
    //---------------------\\
    
    /** The color of the grid. */
    private static Color GRID_COLOR = new Color(211,211,211);
    
    /** The radius of the circle on the Turtle (drawn if isPenDown()). */
    private static final int CIRCLE_RADIUS = 4;
    
    /** Image files (separate images for content and grid). */
    private BufferedImage background, grid, imgTurtle;
    
    /** References to Graphics2D objects for background and grid. */
    private Graphics2D backgroundGraphics, gridGraphics;
    
    /** Collection of all Actors in this JPanel. Is a Set to avoid duplication. */
    private Set<Actor> actors;
    
    /** Width and height of this ActorPanel (in pixels). */
    private int width, height;
    
    //---------------------\\
    //---  CONSTRUCTOR  ---\\
    //---------------------\\
    /**
     * Create a new ActorPanel.
     * An ActorPanel draws the Actors (turtles), which have been added to this ActorPanel.
     * @param   width   The width in pixels of this ActorPanel.
     * @param   height  The height in pixels of this ActorPanel.
     */
    public ActorPanel(int width, int height) throws IOException {
        //Load turtle.png image from disk (must be in the same folder as package.bluej)
        imgTurtle = ImageIO.read(new File("turtle.png"));
        
        //Initialize Actor Set
        actors = new HashSet<Actor>();
        
        //Set width and height
        this.width  = width;
        this.height = height;
        
        //Initialize the background image
        background = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        
        //Reference its Graphics2D object
        backgroundGraphics = background.createGraphics();
        
        //Enable anti-aliasing (makes lines look smoother)
        backgroundGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                            RenderingHints.VALUE_ANTIALIAS_ON);
                                            
        //Initialize the grid image, reference graphics and enable anti-aliasing
        grid = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        gridGraphics = grid.createGraphics();
        gridGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                      RenderingHints.VALUE_ANTIALIAS_ON);
        
        //Set the color of the color
        gridGraphics.setColor(GRID_COLOR);
        
        //Clear both the background and the grid (if not, the screen is just black)
        clear();
        clearGrid();
    }
    
    //---------------------\\
    //-----  SETTERS  -----\\
    //---------------------\\
    
    /**
     * Clears the grid of this ActorPanel (that is, removes it).
     */
    public void clearGrid() {
        //Fill the screen with white
        gridGraphics.setColor(Color.WHITE);
        gridGraphics.fillRect(0, 0, width, height);
        
        //Set the color back, and repaint
        gridGraphics.setColor(GRID_COLOR);
        repaint();
    }
    
    /**
     * Clears the content of this ActorPanel (removes all lines the Actors have drawn).
     */
    public void clear() {
        //fill the screen with transparent pixels
        backgroundGraphics.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
        backgroundGraphics.fillRect(0, 0, width, height);

        //reset composite and repaint
        backgroundGraphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
        repaint();
    }
    
    /**
     * Adds an Actor to this ActorPanel.
     * @param   a   The Actor object.
     */
    public void addActor(Actor a) {
        actors.add(a);
    }
    
    /**
     * Draws a line from (x1, y1) to (x2, y2) using col as Color.
     * @param   x1  The first x-coordinate.
     * @param   y1  The first y-coordinate.
     * @param   x2  The second x-coordinate.
     * @param   y2  The second y-coordinate.
     * @param   col The Color to use.
     */
    public void drawLine(int x1, int y1, int x2, int y2, Color col) {
        backgroundGraphics.setColor(col);
        backgroundGraphics.drawLine(x1, y1, x2, y2);
    }
    
    /**
     * Draws a grid with grid spacing i.
     * @param   i   The spacing (in pixels) between grid lines.
     */
    public void drawGrid(int i) {
        //Draw vertical lines
        for(int x=0; x<=width; x+=i){
            gridGraphics.drawLine(x, 0, x, height);
        }
        
        //Draw horizontal lines
        for(int y=0; y<=height; y+=i){
            gridGraphics.drawLine(0, y, width, y);
        }
        
        //Repaint component
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        //Clear the screen
        super.paintComponent(g);
        
        //Get the Graphics2D object and enable anti-aliasing
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_ON);
        
        //Draw the grid (including white background)
        g2d.drawImage(grid, 0,0, this);
        
        //Draw the lines of the turtle
        g2d.drawImage(background, 0,0, this);
        
        //Draw each Actor
        for(Actor a : actors) {
            //Create the AffineTransform
            AffineTransform trans = new AffineTransform();
            trans.rotate(Math.toRadians(a.getAngle()), a.getX(), 
                                                       a.getY());
            //Apply it to the graphics
            g2d.transform(trans);
            
            //Draw the turtle with center at (x,y)
            g2d.drawImage(imgTurtle, (int)a.getX() - imgTurtle.getWidth()/2, (int)a.getY() - imgTurtle.getHeight()/2, null);
            
            //Draw circle if penDown
            if(a.isPenDown()) {
                //Set the color of the Actor
                g2d.setColor(a.getColorInstance());
                
                //Draw circle
                g2d.fill(new Ellipse2D.Double((int)a.getX() - CIRCLE_RADIUS, (int)a.getY() - CIRCLE_RADIUS, 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS));
            }
        }
    }
}