package package1;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.h2.tools.DeleteDbFiles;

//this class is the welcoming page
public class Login extends JFrame{

	//ATTRIBUTES
	/**
	 * 
	 */
	private static final long serialVersionUID = 202108100L;
	JPanel borderLayout,north, south, center, west;
	JLabel textNorth, textSouth;
	JButton login, clear, newUser;
	JCheckBox showPswd;
	JLabel userLabel, pswdLabel, background;
	JTextField userTextField;
	JPasswordField pswdField;
	
	//attributes to create a new user, starting with nu_
	JPanel nu_panel;
	JLabel nu_userLabel, nu_pswdLabel;
	JTextField nu_userTextField;
	JButton nu_submit, nu_cancel;
	JCheckBox nu_showPswd;
	JPasswordField nu_pswdField;
	
	//modern way to lunch the application
//	public static void main(String args[]) {
//	    java.awt.EventQueue.invokeLater(new Runnable() {
//	        public void run() {
//	            new Login().setVisible(true);
//	        }
//	    });
//	}

	public Login() throws Exception {
		// TODO Auto-generated constructor stub
		
				/*******************CONNECT TO THE DATABASE*************************/
				Connection connection = this.getConnection();
				Statement stat = connection.createStatement();
			    stat.execute("create table if not exists Sutrauser(" //create the table user == sutraUser
			    		+    "username varchar(255) primary key,"
			    		+ 	 "password varchar(20) not null);");
			    
		
				//#############################
				int X = this.getToolkit().getScreenSize().width;  //SCREEN
				int Y = this.getToolkit().getScreenSize().height; //DIMENSIONS
				//#############################
				this.setTitle("SUTRA: Voyage Management App");
				this.setDefaultCloseOperation(EXIT_ON_CLOSE);
				this.setSize((int) (X  * 0.8), (int) (Y * 0.8));
				this.setResizable(false);
				this.setLocation(160,100);
				
				borderLayout = new JPanel(new BorderLayout());
				borderLayout.setBackground(Color.red);
				
				///////////////////////////////////////////
				//northern part
				north = new JPanel(new FlowLayout(1,20,20));
				north.setBackground(Color.RED);
				textNorth = new JLabel("Login to the application to manage your voyages");
				textNorth.setForeground(Color.BLUE);
				textNorth.setFont(new Font("Tahoma", Font.ITALIC, 24));
				textNorth.setBorder(BorderFactory.createTitledBorder("Sutra"));
				north.add(textNorth);
				borderLayout.add(north, BorderLayout.NORTH);
				
				//////////////////////////////////////////////
				//southern part
				south = new JPanel(new FlowLayout());
				south.setBackground(Color.BLACK);
				textSouth = new JLabel("<html> SUTRA: Freight Forwarding & Customs Brokerage <br/>"
						+ "&nbsp; &nbsp; &nbsp; &nbsp;&nbsp; &nbsp; &nbsp; &nbsp;&nbsp; &nbsp; &nbsp;&nbsp; &nbsp; &nbsp;"
						+ "All rights reserved &copy;</html>");
				textSouth.setForeground(new Color(180,180,180));
				textSouth.setFont(new Font("Arial",1,12));
				south.add(textSouth);
				south.add(new JLabel(""));
				borderLayout.add(south, BorderLayout.SOUTH);
				
				/////////////////////////////////////////////
				//west
				background = new JLabel("",new ImageIcon("./images/logo_boat.png")
						, JLabel.CENTER);
				borderLayout.add(background, BorderLayout.WEST);
				
				/////////////////////////////////////////////
				//center
				center = new JPanel(new GridLayout(0,4));
				center.setBackground(new Color(40, 140, 210));
				login = new JButton("<html><u>LOGIN<u></html>");
				login.setBackground(Color.GREEN);
				login.setForeground(Color.BLACK);
				login.addActionListener(new ActionListener() {//checking the database (check are the username and pswd correct)
					
					@Override
					public void actionPerformed(ActionEvent arg0) {
						// TODO Auto-generated method stub
						if(userTextField.getText().length() < 6 || userTextField.getText().length() > 255
								|| pswdField.getPassword().length < 8 || pswdField.getPassword().length > 20)
						{
							JOptionPane.showMessageDialog(null, "<html>Please enter a valid username <i><b>[between 6 and 255 characters]</i></b> <br/> and a valid password <i><b>[between 8 and 20 characters]</i></b></html>");
						}
						else {
							ResultSet usrRes = null;
							ResultSet passRes = null;
							String usr = new String(userTextField.getText());
							String pass = new String(pswdField.getPassword());
							boolean wrongInfo = true;
							boolean emptyResult = true;
							
							try {
								usrRes = connection.createStatement().executeQuery("select username from sutraUser;");
							} catch (SQLException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							try {
									while(usrRes.next())
									{
										emptyResult = false;
										if(usr.equals(usrRes.getString("username"))) //use equals instead of ==
										{
											passRes = connection.createStatement().executeQuery("select password from sutraUser where username='" + usr + "';");
											//stat.executeQuery didn't work, it says the object is already closed
											if(passRes.next())
											{
												if(pass.equals(passRes.getString("password")))
												{
													wrongInfo=false;
													SwingUtilities.getWindowAncestor(login).dispose();
													new AppBody(getUsername()); //to be able to know who is the user
																				//of the app
													JOptionPane.showMessageDialog(null, "<html><h2>Login succeeded, welcome to SUTRA Voyage Management App</html></h2>");
													break;
												}
											}
										}
									}
									if(wrongInfo && !emptyResult)
									{
										JOptionPane.showMessageDialog(null, "<html><h2>Username or Password is not valid! try again...</html></h2>");
									}
									
									//********when the database is empty***************
									if(emptyResult)
									{
											JOptionPane.showMessageDialog((JFrame) SwingUtilities.getWindowAncestor(login), "<html><h2>Please sign up first...<br/>Hit the New User button</html></h2>");
									}
									//*************************************************
							} catch (HeadlessException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							} catch (SQLException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
				}});
				clear = new JButton("<html><u>CLEAR<u></html>");
				clear.setBackground(Color.RED);
				clear.setForeground(Color.BLACK);
				clear.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent arg0) {
						// TODO Auto-generated method stub
						userTextField.setText("");
						pswdField.setText("");
					}
				});
				newUser = new JButton("<html><b>+</b> NEW <b>USER</b></html>");
				newUser.setBackground(new Color(20,0,150));
				newUser.setForeground(Color.WHITE);
				newUser.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent arg0) {
						// TODO Auto-generated method stub
						JFrame This = (JFrame) SwingUtilities.getWindowAncestor(newUser);// This frame == this (that is calling constructor)
						JDialog D = new JDialog(This, "Create a new Account");
						D.setBounds(500,200,600,200);
						D.setVisible(true);
//						D.getRootPane().setDefaultButton(nu_submit);
						
						nu_panel = new JPanel(new GridLayout(0,4));
						nu_panel.setBackground(Color.LIGHT_GRAY);
						
						nu_userLabel = new JLabel("<html><h2>Username:</h2></html>:");
						nu_userLabel.setForeground(Color.BLACK);
						nu_pswdLabel = new JLabel("<html><h2>Password:</h2></html>: ");
						nu_pswdLabel.setForeground(Color.BLACK);
						
						nu_userTextField = new JTextField();
						nu_userTextField.setFont(new Font("Times New Roman", Font.PLAIN, 20));
						
						nu_pswdField = new JPasswordField();
						nu_pswdField.setFont(new Font("Times New Roman", Font.PLAIN, 20));
						nu_pswdField.setEchoChar('*');
						nu_showPswd = new JCheckBox("show password");
						nu_showPswd.setForeground(Color.black);
						nu_showPswd.setBackground(nu_panel.getBackground());
						nu_showPswd.addItemListener(new ItemListener() {
							
							@Override
							public void itemStateChanged(ItemEvent e) {
								// TODO Auto-generated method stub
								if(e.getStateChange()==1)
									nu_pswdField.setEchoChar('\u0000');
								else
									nu_pswdField.setEchoChar('*');
							}
						});
						nu_submit = new JButton("<html><u>Submit</html></u>");
						nu_submit.setBackground(Color.BLUE);
						nu_submit.setForeground(Color.WHITE);
						nu_submit.addActionListener(new ActionListener() {//adding a user to the database
							
							@Override
							public void actionPerformed(ActionEvent arg0) {
								// TODO Auto-generated method stub
								if(nu_userTextField.getText().length() >=6 && nu_userTextField.getText().length() <= 255
										&& nu_pswdField.getPassword().length >=8 && nu_pswdField.getPassword().length <=20)
								{
										boolean userExists=false;
										ResultSet res=null;
										try {
											res = stat.executeQuery("select username from sutraUser;");
										} catch (SQLException e1) {
											// TODO Auto-generated catch block
											e1.printStackTrace();
										}
										try {
											String s = new String(nu_userTextField.getText());
											while(res.next())
											{
												if(s.equals(res.getString("username"))) //use equals instead of ==
												{
													userExists=true;
													JOptionPane.showMessageDialog(D, "username already exists! please login");
													break;
												}
											}
										} catch (HeadlessException e1) {
											// TODO Auto-generated catch block
											e1.printStackTrace();
										} catch (SQLException e1) {
											// TODO Auto-generated catch block
											e1.printStackTrace();
										}
										if(!userExists) {
											try {
												String pass = new String(nu_pswdField.getPassword());
												String user = new String(nu_userTextField.getText());
												stat.execute("insert into sutraUser(username, password) values('"+user+"','"+pass+"');"); //stupid of me to forget '' in varchar!
												JOptionPane.showMessageDialog(D, "<html><h2>Successfully registered</h2></html>");
												D.dispose();
											} catch (SQLException e) {
												// TODO Auto-generated catch block
												JOptionPane.showMessageDialog(D, "Could not register, try again later..");
												e.printStackTrace();
											}
									}
								}
								else{
									JOptionPane.showMessageDialog(D, "<html>Please enter a valid username <i><b>[between 6 and 255 characters]</i></b> <br/> and a valid password <i><b>[between 8 and 20 characters]</i></b></html>");
								}
							}
						});
						nu_cancel = new JButton("<html><u>Cancel</html></u>");
						nu_cancel.setBackground(Color.RED);
						nu_cancel.setForeground(Color.WHITE);
						nu_cancel.addActionListener(new ActionListener() {
							
							@Override
							public void actionPerformed(ActionEvent arg0) {
								// TODO Auto-generated method stub
								D.dispose();
							}
						});
						
						nu_panel.add(new JLabel(""));
						nu_panel.add(nu_userLabel);
						nu_panel.add(nu_userTextField);
						nu_panel.add(new JLabel(""));
						
						nu_panel.add(new JLabel(""));
						nu_panel.add(nu_pswdLabel);
						nu_panel.add(nu_pswdField);
						nu_panel.add(nu_showPswd);

						nu_panel.add(new JLabel(""));
						nu_panel.add(nu_cancel);
						nu_panel.add(nu_submit);
						nu_panel.add(new JLabel(""));
						
						D.add(nu_panel);
					}
				});
				userLabel = new JLabel();
				userLabel.setText("<html><h2>Username:</h2></html>");
				userLabel.setForeground(Color.black);
				pswdLabel = new JLabel();
				pswdLabel.setText("<html><h2>Password:</h2></html>: ");
				pswdLabel.setForeground(Color.black);
				userTextField = new JTextField();
				userTextField.setFont(new Font("Times New Roman", Font.PLAIN, 24));
				pswdField = new JPasswordField();
				pswdField.setEchoChar('*');
				pswdField.setFont(new Font("Times New Roman", Font.PLAIN, 24));
				pswdField.addKeyListener(new KeyAdapter() {
					@Override
					public void keyPressed(KeyEvent e) {
						// TODO Auto-generated method stub
						super.keyPressed(e);
						
					}
				});
				showPswd = new JCheckBox("show password");
				showPswd.setBackground(center.getBackground());
				pswdField.setEchoChar('*');
				showPswd.addItemListener(new ItemListener() {
					
					@Override
					public void itemStateChanged(ItemEvent e) {
						// TODO Auto-generated method stub
						if(e.getStateChange()==1)
							pswdField.setEchoChar('\u0000');
						else
							pswdField.setEchoChar('*');
					}
				});

				//adding components to the center panel
				this.manageSpace(9);
				
				center.add(userLabel);
				center.add(userTextField);
				
				this.manageSpace(2);

				center.add(pswdLabel);
				center.add(pswdField);
				center.add(showPswd);
				
				this.manageSpace(2);

				center.add(clear);
				this.manageSpace(3);
				center.add(login);
				this.manageSpace(7);
				center.add(newUser);
				
				this.manageSpace(8);

				borderLayout.add(center, BorderLayout.CENTER);
				
				
				this.getRootPane().setDefaultButton(login); //THE BUTTON THAT GETS HIT ONCE ENTER IS PRESSED (login)
				this.setContentPane(borderLayout);
				this.setVisible(true);
				
	}//end constructor
	
	//methods
	private Connection getConnection() throws Exception {
	    DeleteDbFiles.execute("~", "test", true);
	    
	    Class.forName("org.h2.Driver");
	    try { Connection conn = DriverManager.getConnection("jdbc:h2:~/sutra","root","pass");
	    	return conn;	    	
	    }catch (Exception e) {
	        e.printStackTrace();
	        return null;
	    }
	}
	
	private String getUsername()
	{
		return this.userTextField.getText();
	}
	
	private void manageSpace(int nb) /*manage space in the center panel*/
	{
		for(int i=0;i<nb;i++)
		{
			center.add(Box.createHorizontalGlue());
		}
	}

}
