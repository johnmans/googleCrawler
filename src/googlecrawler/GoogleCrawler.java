package googlecrawler;

import java.io.IOException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.io.PrintWriter;
import frames.TypicalFrame;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashSet;
import java.util.ListIterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Search Google example. kma
 *
 */
public class GoogleCrawler implements ActionListener, WindowListener, KeyListener, PopupMenuListener, MouseListener, FocusListener, DocumentListener {
    static WebDriver driver;
    static Wait<WebDriver> wait;
    
    private static String last24 = "last_24";
    private static String lastWeek = "last_week";
    private static String lastMonth = "last_month";
    private static String last6months = "last_6_months";
    private static String lastYear = "last_year";
    private static String whenever = "whenever";
    
    private JTextField searchInput = null;
    private JButton goSearch = null;
    private String dateRange = null;
    private static Calendar today = Calendar.getInstance();
    private static Calendar secondDate = Calendar.getInstance();
    private Color bgColor = new Color(240, 240, 240);
    private JTextField rawFileTextfield = null;
    private String rawFileName = "html.txt";
    private JTextField regexFileTextfield = null;
    private String regexFileName = "result.txt";
    private JButton regexApply = null;
    private JComboBox regexCombo = null;
    private boolean isPopUpVisible = false;
    private JButton comboButton = null;
    private String[][] regexTable = null;
    private int numberOfRegexElements = 0;
    private int htmlFileSize = 0;
    private int regexResults = 0;

    public static void main(String[] args) {
        
        boolean serverStarted = false;
        Process seleniumServer = null;
        String consoleOutput = null;

        try {
            byte buffer[] = new byte[1028];
            seleniumServer = Runtime.getRuntime().exec("java -jar bin/selenium-server-standalone.jar");
            while (!(consoleOutput = new String(buffer, "UTF-8")).contains("Started org.openqa.jetty.jetty.Server")) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    System.out.println("who the hell interrupted me?");
                }
                seleniumServer.getInputStream().read(buffer);
                System.out.println(consoleOutput);
            }
            serverStarted = true; //yoo - hoo, we did it
            System.out.println("Server started");

        } catch (IOException ex) {
            System.out.println("sth went awfully wrong");
        }
        
        GoogleCrawler ec = new GoogleCrawler();
        ec.frameInit();
        
    }

    private boolean crawlTheWeb(String email) {
        //type search query
        driver.findElement(By.name("q")).sendKeys(email);

        // click search
        driver.findElement(By.name("btnG")).click();

        // Wait for search to complete
        wait.until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver webDriver) {
                return webDriver.findElement(By.id("resultStats")) != null;
            }
        });
        
        
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(rawFileName, "UTF-8");
            writer.print(driver.getPageSource());
        } catch (Exception e) {}
        
        String checkText = null;
        int index;
        
        for (int i=2; i<=999; i++) {
            index = 0;
            
            while (true) {
                try {
                    driver.findElement(By.id("navcnt"));
                    driver.findElement(By.id("resultStats"));
                    if (checkText != null) {
                        System.out.println(checkText);
                        System.out.println(driver.findElement(By.id("resultStats")).getText());
                        System.out.println("---");
                        if (checkText.equals(driver.findElement(By.id("resultStats")).getText())) {
                            throw new Exception();
                        }
                    }
                    
                    if (index < 10) {
                        index++;
                        driver.findElement(By.linkText("" + i));
                    } else 
                        return true;
                    
                    break;
                } catch(Exception ex) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex1) {
                        System.out.println("whatever");
                    }
                }
            }
 
            writer.append(driver.getPageSource());

            checkText = driver.findElement(By.id("resultStats")).getText();
            driver.findElement(By.linkText("" + i)).click();
        }
        
        writer.close();
        return true;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        secondDate = Calendar.getInstance();
        
        if (e.getActionCommand().equals("last_24")) {
            secondDate.roll(Calendar.DAY_OF_MONTH, false);
        } else if (e.getActionCommand().equals("last_week")) {
            secondDate.roll(Calendar.WEEK_OF_MONTH, false);
        } else if (e.getActionCommand().equals("last_month")) {
            secondDate.roll(Calendar.MONTH, false);
        } else if (e.getActionCommand().equals("last_6_months")) {
            for (int i=0; i<6; i++) {
                secondDate.roll(Calendar.MONTH, false);
            }
        } else if (e.getActionCommand().equals("last_year")) {
            secondDate.roll(Calendar.YEAR, false);
        } else if (e.getActionCommand().equals("whenever")) {
            for (int i=0; i<10; i++) {
                secondDate.roll(Calendar.YEAR, false);
            }
        } else if (e.getActionCommand().equals("go_search")) {
                
            String searchString = searchInput.getText() + " daterange:" + dateRange;

            driver = new FirefoxDriver();
            wait = new WebDriverWait(driver, 30);
            driver.get("http://www.google.com/");

            boolean result;
            try {
                result = crawlTheWeb(searchString);
            } catch(Exception ex) {
                ex.printStackTrace();
                result = false;
            } finally {
                driver.close();

                String messageString = "<html><p style=\"font-family:arial;font-size:9px;\">Created file:<br><br>"
                        + "<u>" + rawFileName + "</u>&nbsp;<font color=#0000FF>(" + new File(rawFileName).length() + " bytes)</font></p></html>";
                JOptionPane.showMessageDialog(null, messageString, "The process has been completed", JOptionPane.INFORMATION_MESSAGE);
            }
            //System.out.println("Search Now!");
        } else if (e.getActionCommand().equals("regex_apply")) {
            if (new File(rawFileName).exists()) {
                regexOnFile();

                String messageString = "<html><p style=\"font-family:arial;font-size:9px;\">Created file:<br><br>"
                        + "<u>" + regexFileName + "</u>&nbsp;<font color=#0000FF>(" + new File(regexFileName).length() + " bytes)</font><br><br>"
                        + "Results: " + regexResults + "</p></html>";
                JOptionPane.showMessageDialog(null, messageString, "The process has been completed", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // do nothing
            }
        }
        
        dateRange = dateToJulian(secondDate.getTime()) + "-" + dateToJulian(today.getTime());
        //System.out.println(dateRange);
    }
    
     private static int compareDates(Date d1, Date d2) {

        Calendar c1 = new GregorianCalendar();
        c1.setTime(d1);
        Calendar c2 = new GregorianCalendar();
        c2.setTime(d2);

        if (c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)) {
          if (c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH)) {
            return c1.get(Calendar.DAY_OF_MONTH) - c2.get(Calendar.DAY_OF_MONTH);
          } else {
            return c1.get(Calendar.MONTH) - c2.get(Calendar.MONTH);
          }
        } else {
          return c1.get(Calendar.YEAR) - c2.get(Calendar.YEAR);
        }
    }
    
    private static String dateToJulian(Date date) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);

        int year;
        int month;
        float day;
        int a;
        int b;
        double d;
        double frac;

        frac = (calendar.get(Calendar.HOUR_OF_DAY) / 0.000024 + calendar.get(Calendar.MINUTE) / 0.001440);

        b = 0;

        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH) + 1;

        DecimalFormat ceroPlaces = new DecimalFormat("0");
        day = calendar.get(Calendar.DAY_OF_MONTH);
        day = Float.parseFloat(ceroPlaces.format(day) + "." + ceroPlaces.format(Math.round(frac)));

        if (month < 3) {
          year--;
          month += 12;
        }
        if (compareDates(calendar.getTime(), calendar.getGregorianChange()) > 0) {
          a = year / 100;
          b = 2 - a + a / 4;
        }
        d = Math.floor(365.25 * year) + Math.floor(30.6001 * (month + 1)) + day + 1720994.5 + b;
        
        int rounded = (int) d;
        return (rounded + ""); 
    } 
    
    public void frameInit() {
        TypicalFrame myFrame = new TypicalFrame("GoogleCrawler", 310, 287);
        myFrame.setIcon(GoogleCrawler.class, "icons/download.png");
        //myFrame.getContentPane().setBackground(bgColor);
        myFrame.setLayout(null);
        myFrame.addWindowListener(this);
        
        searchInput = new JTextField();
        searchInput.setBounds(10, 10, myFrame.getWidth() - 30, 30);
        searchInput.setFont(new Font("Arial", Font.PLAIN, 20));
        searchInput.addFocusListener(this);
        
        JRadioButton last24_but = new JRadioButton("24 hours");
        last24_but.setActionCommand(last24);
        
        JRadioButton lastWeek_but = new JRadioButton("1 week");
        lastWeek_but.setActionCommand(lastWeek);
        
        JRadioButton lastMonth_but = new JRadioButton("1 month");
        lastMonth_but.setActionCommand(lastMonth);
        lastMonth_but.setSelected(true);
        
        JRadioButton last6months_but = new JRadioButton("6 months");
        last6months_but.setActionCommand(last6months);
        last6months_but.setSelected(true);
        
        JRadioButton lastYear_but = new JRadioButton("1 year");
        lastYear_but.setActionCommand(lastYear);
        
        JRadioButton whenever_but = new JRadioButton("10 years");
        whenever_but.setActionCommand(whenever);
        
        JRadioButton jrbArray[] = new JRadioButton[6];
        jrbArray[0] = last24_but;
        jrbArray[1] = lastWeek_but;
        jrbArray[2] = lastMonth_but;
        jrbArray[3] = last6months_but;
        jrbArray[4] = lastYear_but;
        jrbArray[5] = whenever_but;
        
        ButtonGroup group = new ButtonGroup();
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBounds(10, 50, 122, 150);
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Ιnveteracy"));
        //panel.setBackground(bgColor);
        
        for (int i=0; i<jrbArray.length; i++) {
            jrbArray[i].addActionListener(this);
            jrbArray[i].setBounds(10, 20 + 20*i, 100, 20);
            jrbArray[i].setFont(new Font("Arial", Font.PLAIN, 14));
            jrbArray[i].setForeground(Color.decode("0x888888"));
            jrbArray[i].setBorderPainted(false);
            jrbArray[i].setFocusPainted(false);
            jrbArray[i].setCursor(new Cursor(Cursor.HAND_CURSOR));
            jrbArray[i].addKeyListener(this);
            jrbArray[i].addFocusListener(this);
            //jrbArray[i].setBackground(bgColor);
            group.add(jrbArray[i]);
            panel.add(jrbArray[i]);
        }
        
        JPanel panel2 = new JPanel();
        panel2.setLayout(null);
        panel2.setBounds(137, 50, 153, 50);
        panel2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Raw data"));
        
        Label fnLabel = new Label("File:");
        fnLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        fnLabel.setForeground(Color.decode("0x888888"));
        //fnLabel.setBackground(Color.red);
        fnLabel.setBounds(10, 20, 41, 18);
        
        rawFileTextfield = new JTextField("html.txt");
        rawFileTextfield.setFont(new Font("Arial", Font.PLAIN, 12));
        rawFileTextfield.setBounds(53, 20, 85, 18);
        rawFileTextfield.addFocusListener(this);
        rawFileTextfield.getDocument().putProperty("parent", rawFileTextfield);
        rawFileTextfield.getDocument().addDocumentListener(this);
                
        panel2.add(fnLabel);
        panel2.add(rawFileTextfield);
        
        JPanel panel3 = new JPanel();
        panel3.setLayout(null);
        panel3.setBounds(137, 100, 153, 100);
        panel3.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Regex"));
        
        regexApply = new JButton();
        regexApply.setBackground(new Color(0x00ffffff, true));
        regexApply.setBorderPainted(false);
        regexApply.setFocusPainted(false);
        regexApply.setContentAreaFilled(false);
        
        try {
            regexApply.setIcon(new ImageIcon(ImageIO.read(getClass().getResource("icons/regex_button.png"))));
            regexApply.setPressedIcon(new ImageIcon(ImageIO.read(getClass().getResource("icons/regex_button_sel.png"))));
            regexApply.setDisabledIcon(new ImageIcon(ImageIO.read(getClass().getResource("icons/regex_button_dis.png"))));
        } catch (IOException ex) {
            Logger.getLogger(GoogleCrawler.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        regexApply.setCursor(new Cursor(Cursor.HAND_CURSOR));
        regexApply.setLayout(null);
        regexApply.setFont(new Font("Arial", Font.PLAIN, 12));
        regexApply.setBounds(10, 20, 128, 18);
        regexApply.setActionCommand("regex_apply");
        regexApply.addActionListener(this);
        JLabel label0 = new JLabel();
        label0.setHorizontalAlignment(SwingConstants.CENTER);
        label0.setForeground(Color.white);
        label0.setFont(new Font("Arial", Font.BOLD, 11));
        label0.setText("Apply regex");
        label0.setBounds(0, 0, 128, 18);
        regexApply.add(label0);
        
        getRegexFromFile("var/regex.def");
        regexCombo = new JComboBox(regexTable[0]);
        regexCombo.addPopupMenuListener(this);
        regexCombo.setUI(NoButtonUI.createUI(regexCombo));
        regexCombo.updateUI();
        regexCombo.setSelectedIndex(0);
        regexCombo.setBounds(10, 45, 128, 18);
        
        comboButton = new JButton();
        comboButton.addMouseListener(this);
        comboButton.setBackground(new Color(0x00ffffff, true));
        comboButton.setBorderPainted(false);
        comboButton.setFocusPainted(false);
        comboButton.setContentAreaFilled(false);
        try {
            comboButton.setIcon(new ImageIcon(ImageIO.read(getClass().getResource("icons/combo_button.png"))));
            comboButton.setPressedIcon(new ImageIcon(ImageIO.read(getClass().getResource("icons/combo_button_sel.png"))));
            comboButton.setDisabledIcon(new ImageIcon(ImageIO.read(getClass().getResource("icons/combo_button_dis.png"))));
        } catch (IOException ex) {
            Logger.getLogger(GoogleCrawler.class.getName()).log(Level.SEVERE, null, ex);
        }
        comboButton.setBounds(120, 45, 18, 18);
        
        Label fnLabel2 = new Label("File:");
        fnLabel2.setFont(new Font("Arial", Font.PLAIN, 12));
        fnLabel2.setForeground(Color.decode("0x888888"));
        //fnLabel.setBackground(Color.red);
        fnLabel2.setBounds(10, 70, 41, 18);
        
        regexFileTextfield = new JTextField("result.txt");
        regexFileTextfield.setFont(new Font("Arial", Font.PLAIN, 12));
        regexFileTextfield.setBounds(53, 70, 85, 18);
        regexFileTextfield.addFocusListener(this);
        regexFileTextfield.getDocument().putProperty("parent", regexFileTextfield);
        regexFileTextfield.getDocument().addDocumentListener(this);
        
        panel3.add(fnLabel2);
        panel3.add(regexFileTextfield);
        panel3.add(regexApply);
        panel3.add(regexCombo);
        panel3.add(comboButton);
        panel3.setComponentZOrder(comboButton, 0);
        panel3.setComponentZOrder(regexCombo, 1);
        
        secondDate.roll(Calendar.MONTH, false);
        dateRange = dateToJulian(secondDate.getTime()) + "-" + dateToJulian(today.getTime());
        
        goSearch = new JButton();
        goSearch.setActionCommand("go_search");
        goSearch.addActionListener(this);
        
        goSearch.setBounds(195, 210, 90, 30);
        goSearch.setBackground(new Color(0x00ffffff, true));
        goSearch.setBorderPainted(false);
        goSearch.setFocusPainted(false);
        goSearch.setContentAreaFilled(false);
        
        try {
            goSearch.setIcon(new ImageIcon(ImageIO.read(getClass().getResource("icons/button.png"))));
            goSearch.setPressedIcon(new ImageIcon(ImageIO.read(getClass().getResource("icons/button_sel.png"))));
            goSearch.setDisabledIcon(new ImageIcon(ImageIO.read(getClass().getResource("icons/button_dis.png"))));
        } catch (IOException ex) {
            Logger.getLogger(GoogleCrawler.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        goSearch.setCursor(new Cursor(Cursor.HAND_CURSOR));
        goSearch.setLayout(null);
        JLabel label = new JLabel();
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setForeground(Color.white);
        label.setFont(new Font("Arial", Font.BOLD, 13));
        label.setText("Search");
        label.setBounds(2, 2, 86, 26);
        goSearch.add(label);
        
        myFrame.add(searchInput);
        myFrame.add(panel);
        myFrame.add(panel2);
        myFrame.add(panel3);
        myFrame.add(goSearch);
        
        searchInput.addKeyListener(this);
        searchInput.requestFocusInWindow();
        
        fnLabel.repaint();
        fnLabel2.repaint();
        myFrame.repaint();
    }
    
    private void repaintComboArrow() {
        if (comboButton == null)
            return;
        
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Thread.sleep(0, 10);
                    comboButton.repaint();
                    Thread.sleep(0, 100);
                    comboButton.repaint();
                    Thread.sleep(1);
                    comboButton.repaint();
                    Thread.sleep(10);
                    comboButton.repaint();
                    Thread.sleep(100);
                    comboButton.repaint();
                    //System.out.println("repainted");
                } catch (InterruptedException ex) {
                    Logger.getLogger(GoogleCrawler.class.getName()).log(Level.SEVERE, null, ex);
                }
                //System.out.println("repaint");
            }
        }).start();
    }
    
    private void getRegexFromFile(String filename) {
        ArrayList<String> regexNames = new ArrayList<String>();
        ArrayList<String> regexExpressions = new ArrayList<String>();
                
        try{
            FileInputStream fstream = new FileInputStream(filename);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            while ((strLine = br.readLine()) != null)   {
                if (strLine.startsWith("name:")) {
                    strLine = strLine.substring(5);
                    strLine = strLine.trim();
                    regexNames.add(strLine);
                }
                if (strLine.startsWith("expression:")) {
                    strLine = strLine.substring(11);
                    strLine = strLine.trim();
                    regexExpressions.add(strLine);
                }
            }
            in.close();
              } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
        
        numberOfRegexElements = regexNames.size();
        
        if (regexNames.size() == regexExpressions.size()) {
            regexTable = new String [2][numberOfRegexElements];
            
            for (int i=0; i<numberOfRegexElements; i++) {
                regexTable[0][i] = regexNames.get(i);
                regexTable[1][i] = regexExpressions.get(i);
            }
        }
        else {
            regexTable = new String[][] {{"foul"}, {"foul"}};
        }
        
    }
    
    private void regexOnFile() {
        regexResults = 0;
        File rawFile = new File(rawFileName);
        htmlFileSize = (int) rawFile.length();

        
        byte buffer[] = new byte[htmlFileSize];
        String rawText = null;
        ArrayList<String> resultList = new ArrayList<String>();
        
        try{
            FileInputStream fstream = new FileInputStream(rawFileName);
            DataInputStream in = new DataInputStream(fstream);
            in.read(buffer);
            in.close();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
        try {
            rawText = new String(buffer, 0, htmlFileSize, "UTF-8");
            buffer = null;
            Runtime.getRuntime().gc();
        } catch (UnsupportedEncodingException ex) {
            System.out.println("Error during byte[] -> String conversion");
        }
        
        Pattern regexPattern = Pattern.compile(regexTable[1][regexCombo.getSelectedIndex()], Pattern.CASE_INSENSITIVE);
//        Pattern regexPattern = Pattern.compile("window");
        Matcher matcher = regexPattern.matcher(rawText);
        while (matcher.find()) {
            resultList.add(matcher.group());
            //System.out.println(matcher.group());
        }
        Set setItems = new LinkedHashSet(resultList);
        resultList.clear();
        resultList.addAll(setItems);
        arrayListToLowerCase(resultList);
        Collections.sort(resultList);
        
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(regexFileName, "UTF-8");
            ListIterator<String> iterator = resultList.listIterator();
            while (iterator.hasNext()) {
                String result = iterator.next();
                writer.println(result);
                //System.out.println(result);
                regexResults++;
            }
        } catch (Exception e) {}
        writer.close();
    }
    
    private static void arrayListToLowerCase(ArrayList<String> strings)
    {
        ListIterator<String> iterator = strings.listIterator();
        while (iterator.hasNext())
        {
            iterator.set(iterator.next().toLowerCase());
        }
    }

    @Override
    public void windowOpened(WindowEvent e) {
        
    }

    @Override
    public void windowClosing(WindowEvent e) {
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet("http://localhost:4444/selenium-server/driver/?cmd=shutDownSeleniumServer");
        HttpResponse response = null;
        try {
            response = httpclient.execute(httpget);
        } catch (Exception ex) {
            
        }
        HttpEntity entity = null;
        try {
            entity = response.getEntity();
        } catch (Exception ex) { return; }
        if (entity != null) {
            InputStream instream = null;
            try {
                instream = entity.getContent();
            } catch (IOException ex) {
                Logger.getLogger(GoogleCrawler.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalStateException ex) {
                Logger.getLogger(GoogleCrawler.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                InputStreamReader inReader = new InputStreamReader(instream);
                BufferedReader bufReader = new BufferedReader(inReader);
                
                String line;
                try {
                    while ((line = bufReader.readLine()) != null) {
                        System.out.println(line);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(GoogleCrawler.class.getName()).log(Level.SEVERE, null, ex);
                }
            } finally {
                try {
                    instream.close();
                } catch (IOException ex) {
                    Logger.getLogger(GoogleCrawler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    @Override
    public void windowClosed(WindowEvent e) {
        
    }

    @Override
    public void windowIconified(WindowEvent e) {
        
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
        repaintComboArrow();
    }

    @Override
    public void windowActivated(WindowEvent e) {
        repaintComboArrow();
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
        
    }

    @Override
    public void keyTyped(KeyEvent e) {
        
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_ENTER:
                goSearch.doClick();
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        
    }

    @Override
    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        isPopUpVisible = true;
    }

    @Override
    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        isPopUpVisible = false;
    }

    @Override
    public void popupMenuCanceled(PopupMenuEvent e) {

    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getSource().equals(comboButton))
        if (!isPopUpVisible) {
            regexCombo.setPopupVisible(true);
        } else {
            regexCombo.setPopupVisible(false);
        } 
    }

    @Override
    public void mousePressed(MouseEvent e) {
    
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        
    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void focusGained(FocusEvent e) {
        repaintComboArrow();
    }

    @Override
    public void focusLost(FocusEvent e) {
        
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        if (e.getDocument().getProperty("parent").equals(regexFileTextfield)) {
           regexFileName = regexFileTextfield.getText();
//           System.out.println(regexFileName);
       } else if (e.getDocument().getProperty("parent").equals(rawFileTextfield)) {
           rawFileName = rawFileTextfield.getText();
//           System.out.println(rawFileName);
       }
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        if (e.getDocument().getProperty("parent").equals(regexFileTextfield)) {
           regexFileName = regexFileTextfield.getText();
//           System.out.println(regexFileName);
       } else if (e.getDocument().getProperty("parent").equals(rawFileTextfield)) {
           rawFileName = rawFileTextfield.getText();
//           System.out.println(rawFileName);
       }
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
       
    }
}
