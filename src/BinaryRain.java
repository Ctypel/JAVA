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
//JDialog类时创建对话框的主要类
public final class BinaryRain extends JDialog {
	    private Dimension size;// Dimension类封装单个对象中组件的高度和宽度（精确到整数）
	    private Color foreground, background;// 前景色、背景色
	    public char[] RAIN_CHARACTERS;// 字符数组
	    private boolean isColorful;// 颜色是否铺满
	    private boolean hasMusic = false;// 是否播放音乐
	    private AudioClip music;// 音频对象
	    private RainPanel panel = new RainPanel();// RainPanel对象
	    private boolean isStart = false;// 是否开始
		private ArrayList<Rain> rains = new ArrayList();
	    private Font rainFont;// 创建文字雨的字体
		private Object url;


		
    public static void main(String[] args) {
        BinaryRain r = new BinaryRain();
        r.setVisible(true);
        r.start();
    }

	
    /**
     * 通过构造方法初始化
     */
    private BinaryRain() {
        try {
            initProperties();
            init();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Failed to init.\n" + ex, "BinaryRain", JOptionPane.ERROR_MESSAGE);
            System.exit(1);// 非正常退出
        }
    }

    private Color getColor(String color) {
        if (color == null || color.isEmpty()) //isEmpty:分配了内存空间，值为空，是绝对的空，是一种有值（值 = 空）  null:是未分配内存空间，无值，是一种无值(值不存在)
            return null;
        if (color.startsWith("#")) {
            int i = Integer.valueOf(color.substring(1), 16);  //str＝str.substring(int beginIndex) 截取掉str从首字母起长度为beginIndex的字符串，将剩余字符串赋值给str
            return new Color(i);
        }
        if (color.matches("[\\d]+[\\p{Blank}]*,[\\p{Blank}]*[\\d]+[\\p{Blank}]*,[\\p{Blank}]*[\\d]+")) {//matches() 方法用于检测字符串是否匹配给定的正则表达式。
        		//   \\d代表数字  \\p之后的颜色代表是不是这种颜色        [\\d]:数字 [\\p{Blank}]：空格或制表符
        	String[] cs = color.split("[\\p{Blank}]*,[\\p{Blank}]"); //split 方法将一个字符串分割为子字符串，然后将结果作为字符串数组返回。
            if (cs.length != 3)
                return null;
            int r = Integer.valueOf(cs[0]);//返回给定参数的原生 Number 对象值，参数可以是原生数据类型, String等。
            int g = Integer.valueOf(cs[1]);
            int b = Integer.valueOf(cs[2]);
            return new Color(r, g, b);
        }
        return null;
    }

    /**
     * 读取配置文件并初始化
     *
     * @throws Exception
     */
    private void initProperties() throws Exception {
        Properties p = new Properties();
        File f = new File(System.getProperty("user.dir") + "/BinaryRainProperties.properties");// 获取配置文件
        //System.getProperty()方法获取系统变量 ，user.dir获取当前用户目录   properties文件是一种配置文件，主要用于表达配置信息
        //文件类型为*.properties，格式为文本文件，文件的内容是格式是"键=值"的格式
        boolean dw = true, dh = true, df = true, db = true, dc = true, dcf = true;
        if (f.exists() && f.isFile()) {//测试此文件目录存在且为标准文件
        	//public boolean isFile()测试此抽象路径名表示的文件是否是一个标准文件。
        	//public boolean exists()测试此抽象路径名表示的文件或目录是否存在。
            p.load(new FileInputStream(f));// 加载属性文件
            // 获取前景色，默认default
            String strFore = p.getProperty("foreground", "default").toLowerCase();//设置前景色 foreground 
            //toLowerCase() 方法用于将大写字符转换为小写。
            if (!strFore.equals("default")) {
                df = false;
                foreground = getColor(strFore);// 获取颜色
                if (foreground == null)
                    foreground = Color.getColor(strFore, Color.GREEN);// 获取颜色对象，默认绿色
            }// 获取背景色，默认default
            String strBack = p.getProperty("background", "default").toLowerCase();  
            if (!strBack.equals("default")) {
                db = false;
                background = getColor(strBack);// 获取颜色
                if (background == null)
                    background = Color.getColor(strBack, Color.BLACK);
            }// 获取颜色对象，默认绿色
            
            // 获取宽度
            size = new Dimension();
            String strW = p.getProperty("width", "default").toLowerCase();
            if (!strW.equals("default")) {
                dw = false;
                size.width = Integer.valueOf(strW);
            }
            
            // 获取高度
            String strH = p.getProperty("height", "default").toLowerCase();
            if (!strH.equals("default")) {
                dh = false;
                size.height = Integer.valueOf(strH);
            }
            
            // 获取字符数组
            String strC = p.getProperty("characters", "default");
            if (!strC.equalsIgnoreCase("default")) {
                dc = false;
                String[] cs = strC.split(",");
                RAIN_CHARACTERS = new char[cs.length];
                for (int i = 0, s = RAIN_CHARACTERS.length; i < s; i++) {
                    RAIN_CHARACTERS[i] = cs[i].charAt(0);
                }
            }
            
            // 判断颜色是否铺满
            String strCF = p.getProperty("colorful", "default");
            if (!strCF.equalsIgnoreCase("default")) {
                dcf = false;
                isColorful = Boolean.parseBoolean(strCF);
            }
            
            // 判断是否播放音乐
            String strM = p.getProperty("music", "default");
            if (!strM.equalsIgnoreCase("default")) {
                File musicFile = new File("D:/strM.wav");
                if (musicFile.exists() && musicFile.isFile())
                    if ((music = Applet.newAudioClip(musicFile.toURI().toURL())) != null)
                        hasMusic = true;
            }
        }
        if (dw & dh)  // 高度和宽度都是default，获取屏幕高和宽
            size = Toolkit.getDefaultToolkit().getScreenSize();
        else if (dw)  //宽度是default，获取屏幕宽度
            size.width = Toolkit.getDefaultToolkit().getScreenSize().width;
        else if (dh)  //高度是default，获取屏幕高度
            size.height = Toolkit.getDefaultToolkit().getScreenSize().height;
        if (df)// 前景色是default
            foreground = Color.GREEN;
        if (db)// 背景色是default
            background = Color.BLACK;
        if (dc) {// 字符数组是的default
            RAIN_CHARACTERS = new char[126 - 33 + 1];
            for (int c = 0, i = 33, l = RAIN_CHARACTERS.length; c < l; c++, i++)
                RAIN_CHARACTERS[c] = (char) i;
        }
        if (dcf)// 颜色铺满是default
            isColorful = false;
    }
  
    private void init() {
        setAlwaysOnTop(true);// 设置窗口靠前
        setResizable(false);// 不能改变大小
        setUndecorated(true);// 设置此frame窗口失去边框和标题栏的修饰（必须在setVisible之前）
        setTitle("Binary Rain");// 设置标题
        // 创建一个BufferedImage对象
        BufferedImage cursor = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB_PRE);
        setCursor(Toolkit.getDefaultToolkit().createCustomCursor(cursor, new Point(8, 8), "Disable Cursor"));// 确认光标的形状
        setSize(size);// 设置窗口大小
        setLocationRelativeTo(null);// 设置窗口相对于指定组件的位置，null表示位于屏幕的中央
        addKeyListener(new KeyAdapter() {// 新增一个按键侦听器
            @Override
            public void keyPressed(KeyEvent event) {
                if ((event.isAltDown() && event.getKeyCode() == KeyEvent.VK_F4) || (event.getKeyCode() == KeyEvent.VK_ESCAPE)) {// 按下Alt+F4或者Esc键
                    setVisible(false);// 设置窗口不可见
                    System.exit(0);// 正常停止程序
                }
            }
        });
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                if (!isRaining())
                    stop();
                System.exit(0);// 正常停止程序
            }
        });
        add(panel, BorderLayout.CENTER);
    }

    /**
     * 重写setVisible方法，当不显示时停止创建文字雨
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
				rainFont = new Font("arial", Font.BOLD, 15); // 创建文字雨的字体
				e.printStackTrace();
			}
        
    }
    
    /**
     * 开始一个新的线程，创建一条文字雨，使用synchronized保证一个时间内只有一条线程可以执行
     */
    private synchronized void newRain() {
        Rain r = new Rain(getRandomLength(), (int) (Math.random() * size.width), (int) (Math.random() * -60 * 15), (int) (Math.random() * 8 + 2), (float) (Math.random() * 10 + 10));
        rains.add(r);
        new Thread(r).start();
    }
    @SuppressWarnings({ "rawtypes", "unchecked" })

    public void start() {
        if (hasMusic) // 播放音乐
            music.loop(); // 循环播放
        for (int c = 0, s = 108; c < s; c++) // 创建108条文字雨
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
     * 获取面板大小
     *
     * @return
     */
    public Dimension getFrameSize() {
        return size;
    }
    
    /**
     * 是否开始创建文字雨
     *
     * @return
     */
    public boolean isRaining() {
        return isStart;
    } 
    
    /**
     * 获取随机长度(10-50)
     *
     * @return
     */
    public int getRandomLength() {
        return (int) (Math.random() * 40 + 10);
    }
    
    /**
     * 获取随机字符串
     *
     * @return
     */
    public String getRandomChar() {
        return String.valueOf(RAIN_CHARACTERS[(int) (Math.random() * RAIN_CHARACTERS.length)]);
    } 
    
    /**
     * 停止文字雨
     */
    private void stop() {
        isStart = false;
        if (hasMusic)
            music.stop();
    }


    /**
     * 创建文字雨面板
     */
    private final class RainPanel extends JPanel {

        public RainPanel() {
        }

        @Override
        public void paint(Graphics g) {
            if (isStart) {
                BufferedImage img = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);// 创建一个BufferedImage对象
                Graphics2D g2 = (Graphics2D) img.getGraphics();// 获取Graphics2D对象
                g2.setColor(background);// 设置颜色
                g2.fillRect(0, 0, size.width, size.height);// 用预定的颜色填充一个矩形
                g2.setColor(foreground);// 设置颜色
                // 克隆所有文字雨信息到Collection中
                @SuppressWarnings("unchecked") 
				Collection<Rain> collection = (Collection<Rain>) rains.clone();
                for (Iterator<Rain> it = collection.iterator(); it.hasNext();) {
                    Rain r = it.next();
                    if (r.isEnd()) {// 该条文字雨已经结束
                        rains.remove(r);// 将该条文字雨从集合中移除
                        newRain();// 创建一条新的文字雨
                        continue;
                    }
                    if (isColorful) {// 颜色铺满
                        g2.setFont(rainFont.deriveFont(r.getSize()));// 设置文字雨文本大小
                        String[] ss = r.getRainChars();// 获取文字雨文本内容
                        int x = r.getX();// 获取文字雨X轴坐标
                        int y = r.getY() - ss.length * 15;// 获取文字雨Y轴坐标
                        for (int i = 0, sss = ss.length; i < sss; i++) {
                            if (i < 7)
                                g2.setColor(COLORS1[i]);
                            else
                                g2.setColor(COLORS1[i % 7]);
                            g2.drawString(ss[i], x, y);
                            y += 15;
                        }
                    } else {
                        g2.setFont(rainFont.deriveFont(r.getSize()));// 设置文字雨文本大小
                        String[] ss = r.getRainChars();// 获取文字雨文本内容
                        int x = r.getX();// 获取文字雨X轴坐标
                        int y = r.getY() - ss.length * 15;// 获取文字雨Y轴坐标
                        for (String s : ss) {
                            g2.drawString(s, x, y);
                            y += 15;
                        }
                    }
                }
                g.drawImage(img, 0, 0, this);// 绘制指定图像
            }
        }
        private final Color[] COLORS1 = new Color[]{ // 文字雨文本颜色集合
            new Color(255, 0, 0),
            new Color(255, 165, 0),
            new Color(255, 255, 0),
            new Color(0, 255, 0),
            new Color(0, 127, 0),
            new Color(0, 127, 255),
            new Color(139, 0, 255),};
    }

    /**
     * 通过线程创建一条条文字雨
     */
    private final class Rain implements Runnable {
        private final String[] rainChars;// 下雨的文本
        private int rainSpeed;// 下雨的速度
        private int rainX, rainY;// 文本坐标系
        private float fontSize;// 文本大小

        /**
         * 初始化一条文字雨
         *
         * @param length 文字雨的文本长度
         * @param x      x坐标
         * @param y      y坐标
         * @param speed  下雨速度
         * @param size   文本大小
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
         * 执行文字雨
         */
        @Override
        public void run() {
            while (isRaining() && rainY < getFrameSize().height + (rainChars.length + 1) * 15) {
                if (rainSpeed <= 0)// 文字雨的速度小于等于0，结束
                    break;
                try {
                    Thread.sleep(rainSpeed);// 睡眠
                } catch (InterruptedException ex) {
                }
                rainY += 2;// 每次向下运动2
            }
            rainSpeed = -1;// 文字雨结束，速度置为-1
        }

        /**
         * 获取文本内容
         *
         * @return
         */
        public String[] getRainChars() {
            return rainChars;
        }

        /**
         * 获取文字雨X轴坐标
         *
         * @return
         */
        public int getX() {
            return rainX;
        }

        /**
         * 获取文字雨Y轴坐标
         *
         * @return
         */
        public int getY() {
            return rainY;
        }

        /**
         * 获取文字雨文本大小
         *
         * @return
         */
        public float getSize() {
            return fontSize;
        }

        /**
         * 判断文字雨是否结束
         *
         * @return
         */
        public boolean isEnd() {
            return rainSpeed <= 0;
        }

    }
}