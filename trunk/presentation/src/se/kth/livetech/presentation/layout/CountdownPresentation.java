package se.kth.livetech.presentation.layout;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.JPanel;

import se.kth.livetech.communication.RemoteTime;
import se.kth.livetech.contest.graphics.ContentProvider;
import se.kth.livetech.contest.graphics.ICPCColors;
import se.kth.livetech.contest.model.Contest;
import se.kth.livetech.contest.model.ContestUpdateEvent;
import se.kth.livetech.contest.model.ContestUpdateListener;
import se.kth.livetech.contest.model.test.TestContest;
import se.kth.livetech.presentation.graphics.Renderable;
import se.kth.livetech.util.Frame;

@SuppressWarnings("serial")
public class CountdownPresentation extends JPanel implements ContestUpdateListener{
	long timeshift;
	Contest c;
	final static int DISPLAY_SECONDS = 99;
	final static int ANIMATE_FROM = 200;
	Row[] rows = new Row[DISPLAY_SECONDS+1];
	
	public CountdownPresentation(Contest c, RemoteTime time) {
		this.c = c;
		timeshift = time.getRemoteTimeMillis() - System.currentTimeMillis();
		this.setBackground(ICPCColors.BG_COLOR_2);
		rows[0] = new Row(ContentProvider.getCountdownRenderable("?", ""));
		for(int i = 1; i <= DISPLAY_SECONDS; ++i) {
			int secs = i;
			String row1Text = ChineseNumerals.moonspeak(secs);
			String row2Text = "" + secs + " [" + ChineseNumerals.pinyin(secs) + "]";
			rows[i] = new Row(ContentProvider.getCountdownRenderable(row1Text, row2Text));
		}
	}

	class Row extends JPanel {
		Renderable content;
		double age;
		Dimension maxSize = new Dimension(400, 300);
		
		public void setAge(double age) {
			this.age = age;
		}
		public double getAge() {
			return this.age;
		}
		public Row(Renderable content) {
			this.content = content;
		}
		
		public void paintComponent(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;
			double ratio = getRatio();
			g2d.setColor(new Color(1,1,1,(float)ratio));
			Dimension dim = new Dimension((int) (maxSize.width*ratio), (int) (maxSize.height*ratio));
			int x = -dim.width/2;
			int y = -dim.height/2;
			
			g2d.translate(x, y);
			content.render(g2d, dim);
			//g2d.drawRect(0, 0, dim.width, dim.height);
			g2d.translate(-x, -y);
		} 
		
		public double getRatio() {
			return Math.exp(-Math.pow(0.7*age, 2));
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D)g;
		Contest c = this.c;

		long startTime = c.getInfo().getStartTime()*1000; //convert to millis
		long currentTime = System.currentTimeMillis() + timeshift;
		long diffMilli = currentTime - startTime;
	
		double ageOffset;
		
		long milliPart = (1000+diffMilli%1000)%1000;
		if (milliPart < ANIMATE_FROM) {
			ageOffset = Math.floor(diffMilli/1000.0); //floor
		} else {
			ageOffset = Math.floor(diffMilli/1000.0) + ((double)(milliPart - ANIMATE_FROM))/(1000 - ANIMATE_FROM);
		}
		
		for(int i = 0; i<=DISPLAY_SECONDS; ++i) {
			rows[i].setAge(i+ageOffset);
		}
		Rectangle bounds = this.getBounds();
//		
//		{
//			//DEBUG
//			int x = (int) (milliPart/1000.0*bounds.width);
//			g2d.setColor(Color.GREEN);
//			g2d.fillRect(x, 0, 100, bounds.height);
//			g2d.drawString("" + diffMilli, 100, 20);
//		}
		
		
		if (diffMilli < 0) {
			g2d.translate(bounds.getCenterX(), bounds.getCenterY());
			for(Row row : rows){
				int x = (int) (row.getAge()*bounds.width/3);
				g2d.translate(x, 0);	
				row.paintComponent(g2d);
				g2d.translate(-x, 0);
			}
			g2d.translate(-bounds.getCenterX(), -bounds.getCenterY());
		} 
		else {
			String row1Text = "Go!";
			String row2Text = "The contest has started";
			
			Renderable r = ContentProvider.getCountdownRenderable(row1Text, row2Text);
			
			Dimension dim = new Dimension(bounds.width/2, bounds.width/3);
			int x = (int) (bounds.getCenterX() - dim.width/2);
			int y = (int) (bounds.getCenterY() - dim.height/2);
			g2d.translate(x, y);
			g2d.setColor(Color.WHITE);
			r.render(g2d, dim);
			g2d.translate(-x, -y);
		}
		
		this.repaint(20);
	}

	@Override
	public void contestUpdated(ContestUpdateEvent e) {
		this.c = e.getNewContest();
		this.repaint();
	}
	
	public static void main(String[] args) {
		TestContest tc = new TestContest(50, 10, 99000);
		Contest c1 = tc.getContest();
		Frame frame = new Frame("Countdown Presentation", new CountdownPresentation(c1, new RemoteTime.LocalTime()));
		frame.setPreferredSize(new Dimension(1024, 768));
		frame.pack();
	}
}