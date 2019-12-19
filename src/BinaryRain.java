import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import java.io.*;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

@SuppressWarnings("serial")
//JDialog��ʱ�����Ի������Ҫ��
public final class BinaryRain extends JDialog {
	    private Dimension size;// Dimension���װ��������������ĸ߶ȺͿ�ȣ���ȷ��������
	    private Color foreground, background;// ǰ��ɫ������ɫ
	    public char[] RAIN_CHARACTERS;// �ַ�����
	    private boolean isColorful;// ��ɫ�Ƿ�����
	    private boolean hasMusic = false;// �Ƿ񲥷�����
	    private AudioClip music;// ��Ƶ����
	    private RainPanel panel = new RainPanel();// RainPanel����
	    private boolean isStart = false;// �Ƿ�ʼ
		private ArrayList<Rain> rains = new ArrayList();
	    private Font rainFont;// ���������������
		private Object url;


		
    public static void main(String[] args) {
        BinaryRain r = new BinaryRain();
        r.setVisible(true);
        r.start();
    }

	
    /**
     * ͨ�����췽����ʼ��
     */
    private BinaryRain() {
        try {
            initProperties();
            init();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Failed to init.\n" + ex, "BinaryRain", JOptionPane.ERROR_MESSAGE);
            System.exit(1);// �������˳�
        }
    }

    private Color getColor(String color) {
        if (color == null || color.isEmpty()) //isEmpty:�������ڴ�ռ䣬ֵΪ�գ��Ǿ��ԵĿգ���һ����ֵ��ֵ = �գ�  null:��δ�����ڴ�ռ䣬��ֵ����һ����ֵ(ֵ������)
            return null;
        if (color.startsWith("#")) {
            int i = Integer.valueOf(color.substring(1), 16);  //str��str.substring(int beginIndex) ��ȡ��str������ĸ�𳤶�ΪbeginIndex���ַ�������ʣ���ַ�����ֵ��str
            return new Color(i);
        }
        if (color.matches("[\\d]+[\\p{Blank}]*,[\\p{Blank}]*[\\d]+[\\p{Blank}]*,[\\p{Blank}]*[\\d]+")) {//matches() �������ڼ���ַ����Ƿ�ƥ�������������ʽ��
        		//   \\d��������  \\p֮�����ɫ�����ǲ���������ɫ        [\\d]:���� [\\p{Blank}]���ո���Ʊ��
        	String[] cs = color.split("[\\p{Blank}]*,[\\p{Blank}]"); //split ������һ���ַ����ָ�Ϊ���ַ�����Ȼ�󽫽����Ϊ�ַ������鷵�ء�
            if (cs.length != 3)
                return null;
            int r = Integer.valueOf(cs[0]);//���ظ���������ԭ�� Number ����ֵ������������ԭ����������, String�ȡ�
            int g = Integer.valueOf(cs[1]);
            int b = Integer.valueOf(cs[2]);
            return new Color(r, g, b);
        }
        return null;
    }

    /**
     * ��ȡ�����ļ�����ʼ��
     *
     * @throws Exception
     */
    private void initProperties() throws Exception {
        Properties p = new Properties();
        File f = new File(System.getProperty("user.dir") + "/BinaryRainProperties.properties");// ��ȡ�����ļ�
        //System.getProperty()������ȡϵͳ���� ��user.dir��ȡ��ǰ�û�Ŀ¼   properties�ļ���һ�������ļ�����Ҫ���ڱ��������Ϣ
        //�ļ�����Ϊ*.properties����ʽΪ�ı��ļ����ļ��������Ǹ�ʽ��"��=ֵ"�ĸ�ʽ
        boolean dw = true, dh = true, df = true, db = true, dc = true, dcf = true;
        if (f.exists() && f.isFile()) {//���Դ��ļ�Ŀ¼������Ϊ��׼�ļ�
        	//public boolean isFile()���Դ˳���·������ʾ���ļ��Ƿ���һ����׼�ļ���
        	//public boolean exists()���Դ˳���·������ʾ���ļ���Ŀ¼�Ƿ���ڡ�
            p.load(new FileInputStream(f));// ���������ļ�
            // ��ȡǰ��ɫ��Ĭ��default
            String strFore = p.getProperty("foreground", "default").toLowerCase();//����ǰ��ɫ foreground 
            //toLowerCase() �������ڽ���д�ַ�ת��ΪСд��
            if (!strFore.equals("default")) {
                df = false;
                foreground = getColor(strFore);// ��ȡ��ɫ
                if (foreground == null)
                    foreground = Color.getColor(strFore, Color.GREEN);// ��ȡ��ɫ����Ĭ����ɫ
            }// ��ȡ����ɫ��Ĭ��default
            String strBack = p.getProperty("background", "default").toLowerCase();  
            if (!strBack.equals("default")) {
                db = false;
                background = getColor(strBack);// ��ȡ��ɫ
                if (background == null)
                    background = Color.getColor(strBack, Color.BLACK);
            }// ��ȡ��ɫ����Ĭ����ɫ
            
            // ��ȡ���
            size = new Dimension();
            String strW = p.getProperty("width", "default").toLowerCase();
            if (!strW.equals("default")) {
                dw = false;
                size.width = Integer.valueOf(strW);
            }
            
            // ��ȡ�߶�
            String strH = p.getProperty("height", "default").toLowerCase();
            if (!strH.equals("default")) {
                dh = false;
                size.height = Integer.valueOf(strH);
            }
            
            // ��ȡ�ַ�����
            String strC = p.getProperty("characters", "default");
            if (!strC.equalsIgnoreCase("default")) {
                dc = false;
                String[] cs = strC.split(",");
                RAIN_CHARACTERS = new char[cs.length];
                for (int i = 0, s = RAIN_CHARACTERS.length; i < s; i++) {
                    RAIN_CHARACTERS[i] = cs[i].charAt(0);
                }
            }
            
            // �ж���ɫ�Ƿ�����
            String strCF = p.getProperty("colorful", "default");
            if (!strCF.equalsIgnoreCase("default")) {
                dcf = false;
                isColorful = Boolean.parseBoolean(strCF);
            }
            
            // �ж��Ƿ񲥷�����
            String strM = p.getProperty("music", "default");
            if (!strM.equalsIgnoreCase("default")) {
                File musicFile = new File("D:/strM.wav");
                if (musicFile.exists() && musicFile.isFile())
                    if ((music = Applet.newAudioClip(musicFile.toURI().toURL())) != null)
                        hasMusic = true;
            }
        }
        if (dw & dh)  // �߶ȺͿ�ȶ���default����ȡ��Ļ�ߺͿ�
            size = Toolkit.getDefaultToolkit().getScreenSize();
        else if (dw)  //�����default����ȡ��Ļ���
            size.width = Toolkit.getDefaultToolkit().getScreenSize().width;
        else if (dh)  //�߶���default����ȡ��Ļ�߶�
            size.height = Toolkit.getDefaultToolkit().getScreenSize().height;
        if (df)// ǰ��ɫ��default
            foreground = Color.GREEN;
        if (db)// ����ɫ��default
            background = Color.BLACK;
        if (dc) {// �ַ������ǵ�default
            RAIN_CHARACTERS = new char[126 - 33 + 1];
            for (int c = 0, i = 33, l = RAIN_CHARACTERS.length; c < l; c++, i++)
                RAIN_CHARACTERS[c] = (char) i;
        }
        if (dcf)// ��ɫ������default
            isColorful = false;
    }
  
    private void init() {
        setAlwaysOnTop(true);// ���ô��ڿ�ǰ
        setResizable(false);// ���ܸı��С
        setUndecorated(true);// ���ô�frame����ʧȥ�߿�ͱ����������Σ�������setVisible֮ǰ��
        setTitle("Binary Rain");// ���ñ���
        // ����һ��BufferedImage����
        BufferedImage cursor = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB_PRE);
        setCursor(Toolkit.getDefaultToolkit().createCustomCursor(cursor, new Point(8, 8), "Disable Cursor"));// ȷ�Ϲ�����״
        setSize(size);// ���ô��ڴ�С
        setLocationRelativeTo(null);// ���ô��������ָ�������λ�ã�null��ʾλ����Ļ������
        addKeyListener(new KeyAdapter() {// ����һ������������
            @Override
            public void keyPressed(KeyEvent event) {
                if ((event.isAltDown() && event.getKeyCode() == KeyEvent.VK_F4) || (event.getKeyCode() == KeyEvent.VK_ESCAPE)) {// ����Alt+F4����Esc��
                    setVisible(false);// ���ô��ڲ��ɼ�
                    System.exit(0);// ����ֹͣ����
                }
            }
        });
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                if (!isRaining())
                    stop();
                System.exit(0);// ����ֹͣ����
            }
        });
        add(panel, BorderLayout.CENTER);
    }

    /**
     * ��дsetVisible������������ʾʱֹͣ����������
     */
    public void setVisibfle(boolean flag) {
        super.setVisible(flag);
        if (!flag)
            stop();
    }

    {
       
            try {
				rainFont = Font.createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream("RainFont.ttf")).deriveFont(Font.BOLD, 15.0f);
			} catch (FontFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				rainFont = new Font("arial", Font.BOLD, 15); // ���������������
				e.printStackTrace();
			}
        
    }
    
    /**
     * ��ʼһ���µ��̣߳�����һ�������꣬ʹ��synchronized��֤һ��ʱ����ֻ��һ���߳̿���ִ��
     */
    private synchronized void newRain() {
        Rain r = new Rain(getRandomLength(), (int) (Math.random() * size.width), (int) (Math.random() * -60 * 15), (int) (Math.random() * 8 + 2), (float) (Math.random() * 10 + 10));
        rains.add(r);
        new Thread(r).start();
    }
    @SuppressWarnings({ "rawtypes", "unchecked" })

    public void start() {
        if (hasMusic) // ��������
            music.loop(); // ѭ������
        for (int c = 0, s = 108; c < s; c++) // ����108��������
            newRain();
        isStart = true;
        for (Rain r : rains)
            new Thread(r).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isStart)
                    panel.repaint();
            }
        }).start();
    }
    
    /**
     * ��ȡ����С
     *
     * @return
     */
    public Dimension getFrameSize() {
        return size;
    }
    
    /**
     * �Ƿ�ʼ����������
     *
     * @return
     */
    public boolean isRaining() {
        return isStart;
    } 
    
    /**
     * ��ȡ�������(10-50)
     *
     * @return
     */
    public int getRandomLength() {
        return (int) (Math.random() * 40 + 10);
    }
    
    /**
     * ��ȡ����ַ���
     *
     * @return
     */
    public String getRandomChar() {
        return String.valueOf(RAIN_CHARACTERS[(int) (Math.random() * RAIN_CHARACTERS.length)]);
    } 
    
    /**
     * ֹͣ������
     */
    private void stop() {
        isStart = false;
        if (hasMusic)
            music.stop();
    }


    /**
     * �������������
     */
    private final class RainPanel extends JPanel {

        public RainPanel() {
        }

        @Override
        public void paint(Graphics g) {
            if (isStart) {
                BufferedImage img = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);// ����һ��BufferedImage����
                Graphics2D g2 = (Graphics2D) img.getGraphics();// ��ȡGraphics2D����
                g2.setColor(background);// ������ɫ
                g2.fillRect(0, 0, size.width, size.height);// ��Ԥ������ɫ���һ������
                g2.setColor(foreground);// ������ɫ
                // ��¡������������Ϣ��Collection��
                @SuppressWarnings("unchecked") 
				Collection<Rain> collection = (Collection<Rain>) rains.clone();
                for (Iterator<Rain> it = collection.iterator(); it.hasNext();) {
                    Rain r = it.next();
                    if (r.isEnd()) {// �����������Ѿ�����
                        rains.remove(r);// ������������Ӽ������Ƴ�
                        newRain();// ����һ���µ�������
                        continue;
                    }
                    if (isColorful) {// ��ɫ����
                        g2.setFont(rainFont.deriveFont(r.getSize()));// �����������ı���С
                        String[] ss = r.getRainChars();// ��ȡ�������ı�����
                        int x = r.getX();// ��ȡ������X������
                        int y = r.getY() - ss.length * 15;// ��ȡ������Y������
                        for (int i = 0, sss = ss.length; i < sss; i++) {
                            if (i < 7)
                                g2.setColor(COLORS1[i]);
                            else
                                g2.setColor(COLORS1[i % 7]);
                            g2.drawString(ss[i], x, y);
                            y += 15;
                        }
                    } else {
                        g2.setFont(rainFont.deriveFont(r.getSize()));// �����������ı���С
                        String[] ss = r.getRainChars();// ��ȡ�������ı�����
                        int x = r.getX();// ��ȡ������X������
                        int y = r.getY() - ss.length * 15;// ��ȡ������Y������
                        for (String s : ss) {
                            g2.drawString(s, x, y);
                            y += 15;
                        }
                    }
                }
                g.drawImage(img, 0, 0, this);// ����ָ��ͼ��
            }
        }
        private final Color[] COLORS1 = new Color[]{ // �������ı���ɫ����
            new Color(255, 0, 0),
            new Color(255, 165, 0),
            new Color(255, 255, 0),
            new Color(0, 255, 0),
            new Color(0, 127, 0),
            new Color(0, 127, 255),
            new Color(139, 0, 255),};
    }

    /**
     * ͨ���̴߳���һ����������
     */
    private final class Rain implements Runnable {
        private final String[] rainChars;// ������ı�
        private int rainSpeed;// ������ٶ�
        private int rainX, rainY;// �ı�����ϵ
        private float fontSize;// �ı���С

        /**
         * ��ʼ��һ��������
         *
         * @param length ��������ı�����
         * @param x      x����
         * @param y      y����
         * @param speed  �����ٶ�
         * @param size   �ı���С
         */
        public Rain(int length, int x, int y, int speed, float size) {
            if (speed < 1)
                throw new RuntimeException("The speed must be greater than or equal to 1.");
            if (length < 5)
                length = getRandomLength();
            if (size < 1.0f)
                size = 15.0f;
            rainChars = new String[length + 1];
            for (int i = 0; i < length; i++)
                rainChars[i] = getRandomChar();
            rainChars[length] = " ";
            this.rainX = x;
            this.rainY = y;
            this.rainSpeed = speed;
            this.fontSize = size;
        }

        /**
         * ִ��������
         */
        @Override
        public void run() {
            while (isRaining() && rainY < getFrameSize().height + (rainChars.length + 1) * 15) {
                if (rainSpeed <= 0)// ��������ٶ�С�ڵ���0������
                    break;
                try {
                    Thread.sleep(rainSpeed);// ˯��
                } catch (InterruptedException ex) {
                }
                rainY += 2;// ÿ�������˶�2
            }
            rainSpeed = -1;// ������������ٶ���Ϊ-1
        }

        /**
         * ��ȡ�ı�����
         *
         * @return
         */
        public String[] getRainChars() {
            return rainChars;
        }

        /**
         * ��ȡ������X������
         *
         * @return
         */
        public int getX() {
            return rainX;
        }

        /**
         * ��ȡ������Y������
         *
         * @return
         */
        public int getY() {
            return rainY;
        }

        /**
         * ��ȡ�������ı���С
         *
         * @return
         */
        public float getSize() {
            return fontSize;
        }

        /**
         * �ж��������Ƿ����
         *
         * @return
         */
        public boolean isEnd() {
            return rainSpeed <= 0;
        }

    }
}