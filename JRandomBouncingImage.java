
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Toolkit;
import java.awt.Image;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.AlphaComposite;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;

public class JRandomBouncingImage extends JFrame {

//  Private variables   //  \\  //  \\  //  \\

Image image;
Color[] colours;



//  Helper functions    //  \\  //  \\  //  \\

private static int randomInteger(int lowerBound, int upperBound) {
	// Upper bound is exclusive, since in this application
	// we use this function basically only for offsets.
	double randomGain = Math.random() * (upperBound - lowerBound);
	return lowerBound + (int)Math.floor(randomGain);
}



//  Private classes     //  \\  //  \\  //  \\

private class Pane extends JPanel implements ActionListener {

	public void actionPerformed(ActionEvent eA) {
		repaint();
	}

	public void paintComponent(Graphics g) {
		g.clearRect(0, 0, getWidth(), getHeight());	
		
		if (image == null) return;
		int gridWidth = image.getWidth(this);
		int gridHeight = image.getHeight(this);
		if (gridWidth == -1) return;
		// Image is still loading, so cancel.

		int gridColumns = getWidth() / gridWidth;
		int gridRows = getHeight() / gridHeight;
		int xOffset = (getWidth() % gridWidth) / 2;
		int yOffset = (getHeight() % gridHeight) / 2;
		
		int bouncingsToGenerate = gridColumns + gridRows;
		// Arbitrary. I haven't researched the actual algorithm		
		Bouncing[] bouncings = new Bouncing[bouncingsToGenerate];
		while (bouncingsToGenerate-- > 0) {
			bouncings[bouncingsToGenerate] = 
				randomBouncing(gridColumns, gridRows);
		}
				
		// Okay, let's start actually drawing.
		
		// Draw to an in-memory buffer first. We're going to use 
		// alpha composites, and we need trasparency for that.
		BufferedImage buffer = new BufferedImage(
			getWidth(), getHeight(),
			BufferedImage.TYPE_INT_ARGB
		);
		Graphics gBuf = buffer.getGraphics();
		Graphics2D g2Buf = (Graphics2D)gBuf;
		for (Bouncing bouncing: bouncings) {
			int normalX = (bouncing.gridX * gridWidth) + xOffset;
			int normalY = (bouncing.gridY * gridHeight) + yOffset;
			
			// Draw the image itself.
			gBuf.drawImage(image, normalX, normalY, this);
			
			// Now fill a coloured rect with SrcIn. Aside from
			// the drawn image, the buffer should be empty pixels (alpha 0),
			// so this should work.
			g2Buf.setComposite(AlphaComposite.SrcIn);
			g2Buf.setColor(bouncing.colour);
			g2Buf.fillRect(
				normalX, normalY, 
				gridWidth, gridHeight
			);
			g2Buf.setComposite(AlphaComposite.SrcOver);
		}
		
		// Alright, now just draw the buffer on top.
		g.drawImage(buffer, 0, 0, this);
	}
	
	private Bouncing randomBouncing(int gridColumns, int gridRows) {
		Bouncing returnee = new Bouncing();
		returnee.gridX = randomInteger(0, gridColumns);
		returnee.gridY = randomInteger(0, gridRows);
		returnee.colour = colours[randomInteger(0, colours.length)];
		return returnee;
	}
	
}

private static class Bouncing {
	int gridX, gridY;
	Color colour;
}



//  Constructors    \\  //  \\  //  \\  //  \\

private JRandomBouncingImage() {
	// Some of our properties first..
	setBackground(Color.BLACK);

	// Now, set up our content pane...
	Pane pane = new Pane();
	setContentPane(pane);
	// Add some sort of input to let this app be closable..
	pane.addMouseMotionListener(
		new MouseMotionListener() {
			public void mouseMoved(MouseEvent eM) {
				// Programatically send a closing event.
				JRandomBouncingImage.this
					.dispatchEvent(
						new WindowEvent(
							JRandomBouncingImage.this,
							/*
							Apparently JPanel us can't be the source,
							it has to be a window. Therefore
							"window tells itself to close".
							*/
							WindowEvent.WINDOW_CLOSING
						)
					);
				// Also, stow away ourselves so that a single
				// sweep of the mouse doesn't create a dozen events.
				// Which would be a huge pain if a close listener
				// were to open a dialog window.
				JRandomBouncingImage.this.setVisible(false);
			}			
			public void mouseDragged(MouseEvent eM) { }
		}
	);
	// Now, (we may) go fullscreen.
	Toolkit toolkit = Toolkit.getDefaultToolkit();	
	if (toolkit.isFrameStateSupported(JFrame.MAXIMIZED_BOTH)) {
		setExtendedState(JFrame.MAXIMIZED_BOTH);
	}
	else {
		setSize(toolkit.getScreenSize());
	}
	setUndecorated(true);
	
	// Load pixmap.
	URL imageURL = getClass().getResource("./image.png");
	if (imageURL == null) {
		// File not found. What to do?		
	}
	image = toolkit.createImage(imageURL);
	
	// Set colours.
	colours = new Color[8];
	for (int o = 0; o < colours.length; ++o) {
		colours[o] = new Color(
			randomInteger(128, 192),
			randomInteger(128, 192),
			randomInteger(128, 192)
		);
	}
	
	// Now start a timer.
	Timer timer = new Timer(3000, pane);	
	timer.start();
}



//  Main    \\  //  \\  //  \\  //  \\  //  \\

public static void main(String... args) {
	JRandomBouncingImage instance = new JRandomBouncingImage();
	instance.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	instance.setVisible(true);
}

}
