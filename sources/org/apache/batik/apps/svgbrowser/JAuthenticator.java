package org.apache.batik.apps.svgbrowser;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JComponent;
import javax.swing.SwingConstants;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * This class is resposible for providing authentication information
 * when needed by network protocols.  It does this by poping up a small
 * window that asks for User ID and password for the system.
 */
public class JAuthenticator extends Authenticator {

    /**
     * Internationalization message string
     */
    public static final String TITLE
        = "JAuthenticator.title";
    public static final String LABEL_SITE
        = "JAuthenticator.label.site";
    public static final String LABEL_REQ
        = "JAuthenticator.label.req";
    public static final String LABEL_USERID
        = "JAuthenticator.label.userID";
    public static final String LABEL_PASSWORD
        = "JAuthenticator.label.password";

    public static final String LABEL_CANCEL
        = "JAuthenticator.label.cancel";
    public static final String LABEL_OK
        = "JAuthenticator.label.ok";

    protected JDialog window;
    protected JButton cancelButton;
    protected JButton okButton;

    protected JLabel     label1;
    protected JLabel     label2;
    protected JTextField     JUserID;
    protected JPasswordField JPassword;

    Object lock = new Object();

    private boolean result;
    private String  userID;
    private char [] password;

    public JAuthenticator() {
        initWindow();
    }

    protected void initWindow() {
        String title = Resources.getString(TITLE);
        window = new JDialog((Frame)null, title, true);

        Container mainPanel = window.getContentPane();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(buildAuthPanel(), BorderLayout.CENTER);
        mainPanel.add(buildButtonPanel(), BorderLayout.SOUTH);
        window.pack();

        window.addWindowListener( new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    cancelListener.actionPerformed
                        (new ActionEvent(e.getWindow(), 
                                         ActionEvent.ACTION_PERFORMED,
                                         "Close"));
                }
            });
    }

    protected JComponent buildAuthPanel() {
        GridBagLayout gridBag = new GridBagLayout();
        GridBagConstraints  c = new GridBagConstraints();
        JPanel proxyPanel = new JPanel(gridBag);
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;

        c.gridwidth = 1;
        JLabel labelS = new JLabel(Resources.getString(LABEL_SITE));
        labelS.setHorizontalAlignment(SwingConstants.LEFT);
        gridBag.setConstraints(labelS, c);
        proxyPanel.add(labelS);
		
        c.gridwidth = GridBagConstraints.REMAINDER;
        label1 = new JLabel("");
        label1.setHorizontalAlignment(SwingConstants.LEFT);
        gridBag.setConstraints(label1, c);
        proxyPanel.add(label1);
		
        c.gridwidth = 1;
        JLabel labelR = new JLabel(Resources.getString(LABEL_REQ));
        labelR.setHorizontalAlignment(SwingConstants.LEFT);
        gridBag.setConstraints(labelR, c);
        proxyPanel.add(labelR);
		
        c.gridwidth = GridBagConstraints.REMAINDER;
        label2 = new JLabel("");
        label2.setHorizontalAlignment(SwingConstants.LEFT);
        gridBag.setConstraints(label2, c);
        proxyPanel.add(label2);
		
        c.gridwidth = 1;
        JLabel labelUserID = new JLabel(Resources.getString(LABEL_USERID));
        labelUserID.setHorizontalAlignment(SwingConstants.LEFT);
        gridBag.setConstraints(labelUserID, c);
        proxyPanel.add(labelUserID);

        c.gridwidth = GridBagConstraints.REMAINDER;
        JUserID = new JTextField(20);
        gridBag.setConstraints(JUserID, c);
        proxyPanel.add(JUserID);

        c.gridwidth = 1;
        JLabel labelPassword = new JLabel(Resources.getString(LABEL_PASSWORD));
        labelPassword.setHorizontalAlignment(SwingConstants.LEFT);
        gridBag.setConstraints(labelPassword, c);
        proxyPanel.add(labelPassword);

        c.gridwidth = GridBagConstraints.REMAINDER;
        JPassword = new JPasswordField(20);
        JPassword.setEchoChar('*');
        JPassword.addActionListener(okListener);
        gridBag.setConstraints(JPassword, c);
        proxyPanel.add(JPassword);

        return proxyPanel;
    }

    

    protected JComponent buildButtonPanel() {
        JPanel buttonPanel = new JPanel();
        cancelButton = new JButton(Resources.getString(LABEL_CANCEL));
        cancelButton.addActionListener(cancelListener);
        buttonPanel.add(cancelButton);

        okButton = new JButton(Resources.getString(LABEL_OK));
        okButton.addActionListener( okListener);
        buttonPanel.add(okButton);

        return buttonPanel;
    }

    /** 
     * This is called by the protocol stack when authentication is
     * required.  We then show the dialog in the Swing event thread,
     * and block waiting for the user to select either cancel or ok,
     * at which point we get notified.
     */
    public PasswordAuthentication getPasswordAuthentication() {
        synchronized (lock) {
            EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        label1.setText(getRequestingSite().getHostName());
                        label2.setText(getRequestingPrompt());
                        window.setVisible(true);
                    }
                });
            try {
                lock.wait();
            } catch(InterruptedException ie) { }
            if (!result)
                return null;

            return new PasswordAuthentication(userID, password);
        }
    }

    ActionListener okListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                synchronized (lock) {
                    window.setVisible(false);

                    userID   = JUserID.getText();
                    password = JPassword.getPassword();
                    JPassword.setText("");
                    result = true;
                    lock.notifyAll();
                }
            }
        };

    ActionListener cancelListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                synchronized (lock) {
                    window.setVisible(false);

                    userID = null; 
                    JUserID.setText("");
                    password = null;
                    JPassword.setText("");
                    result = false;
                
                    lock.notifyAll();
                }
            }
        };
}
