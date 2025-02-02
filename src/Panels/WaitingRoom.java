package Panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.font.TextAttribute;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableModel;

import Core.Starter;
import Engines.CustomTextPane;
import Engines.EpicEngine;
import Engines.PageControl;
import Global.Constants;
import Global.Functions;
import Global.ImageManager;
import Global.SoundManager;
import Global.Variables;
import Global.Constants.GameMode;
import Network.InnerData.WaitingRoomInfo;
import Network.NetworkCore.GameClient;
import Network.NetworkCore.GameServer;
import Network.NetworkCore.NetworkTag;
import Network.Objects.Chat;
import Network.Objects.User;
import Objects.Coordinate;
import Utility.EnginesControl;
import Utility.TriggeredButton;
import Utility.TriggeredTextArea;
import Utility.onButtonListener;
import Utility.TriggeredTextArea.EnterListener;

public class WaitingRoom extends JPanel implements PageControl{
		//Necessary
		public static Dimension PanelSize;
		public static boolean isActivated;
		
		//Utility
		private static Functions ff = new Functions();
		private static EnginesControl ect = new EnginesControl();
		
		private GameMode gameMode = GameMode.SummonersRift;
		
		private static TriggeredButton HomeBtn, CloseBtn, CancelBtn, RealGameStartBtn;
		private static TriggeredTextArea Chatr = new TriggeredTextArea(new Rectangle(45, 670, 300, 30));
		
		private String Roomname;
		private String password;
		private boolean isGameHost;
		
		private GameServer gameServer;							//게임 서버, 호스트일 경우에만 활성화
		private GameClient gameClient = new GameClient();;		//게임 클라이언트 무조건 활성화
		
		private ArrayList<User> userList1 = new ArrayList<>(), userList2 = new ArrayList<>();
		private int chatSize = 0;
		
		private WaitingRoomInfo wri = new WaitingRoomInfo("", "");
		
		private static CustomTextPane chatArea = new CustomTextPane(true);
		private static JScrollPane scrollPane;
		
		private static TriggeredButton[][] MoveTeamBtn;
		
		public void paintComponent(Graphics graphics) {
			Graphics2D g = (Graphics2D) graphics;
			
			g.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			
			g.drawImage(ImageManager.ClientTemplateImage, null, 0, 0);
			
			g.setColor(Color.WHITE);
			g.setFont(ff.getClassicFont(16F, true));
			g.drawString(Variables.Username, 1127, 40);
			
			g.drawImage(ImageManager.WaitingRoomImage,null,0,0);
			
			g.setColor(new Color(65,60,70,255));
			g.setFont(ff.getFancyFont(13F, true));
			ect.fde.drawCenteredString(g, Constants.ProgramVersion, new Rectangle(1060, 688, 220, 32));
			
			g.setColor(new Color(240, 230, 210));
			g.setFont(ff.getClassicFont(27F, true));
			g.drawString(wri.getRoomName(), 90, 120);
			
			g.setColor(new Color(170, 165, 119));
			g.setFont(ff.getClassicFont(13F, true));
			switch(gameMode) {
			case SummonersRift:
				g.drawImage(ImageManager.SRicon, null, 40,100);
				g.drawString("소환사의 협곡", 90, 143);
				break;
			case KnifeWind:
				g.drawImage(ImageManager.KWicon, null, 40,100);
				g.drawString("칼바람 나락", 90, 143);
				break;
			case URF:
				g.drawImage(ImageManager.URFicon, null, 40,100);
				g.drawString("U.R.F", 90, 143);
				break;
			}
			
			g.setFont(ff.getClassicFont(16F, true));
			
			g.drawString("1팀", 70, 200);
			g.drawString("2팀", 970, 200);
			
			g.setColor(new Color(240, 230, 210));
			
			try {
				g.drawString("Host IP address : "+InetAddress.getLocalHost().getHostAddress(), 630, 510);
				g.drawString("Public IP address : "+Constants.publicIP, 630, 530);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			
			g.setColor(new Color(70,70,70));
			
			
			for(int i=0;i<5;i++)
				for(int j=0;j<2;j++)
					g.drawLine(50+495*j, 260+50*i, 530+495*j, 260+50*i);
			
			g.setColor(new Color(240, 230, 210));
			
			
			FontMetrics metrics = g.getFontMetrics(g.getFont());
			for(int i=0;i<5;i++) {
				g.setColor(new Color(240, 230, 210));
				if(i<userList1.size()) {
					String name = userList1.get(i).getUserName();
					g.drawString(name, 60, 245+50*i);
					if(userList1.get(i).isGameHost())
						g.drawImage(ImageManager.GameHostSymbol, null, 60+metrics.stringWidth(name)+15, 245+50*i-15);
				}else {
					g.setColor(new Color(80, 80, 80));
					g.drawString("비어있음", 60, 245+50*i);
				}
			}
			for(int i=0;i<5;i++) {
				g.setColor(new Color(240, 230, 210));
				if(i<userList2.size()) {
					String name =userList2.get(i).getUserName();
					g.drawString(name, 60+495, 245+50*i);
					if(userList2.get(i).isGameHost())
						g.drawImage(ImageManager.GameHostSymbol, null, 60+495+metrics.stringWidth(name)+15, 245+50*i-15);
				}else {
					g.setColor(new Color(80, 80, 80));
					g.drawString("비어있음", 60+495, 245+50*i);
				}
			}
			
			
			if(this.isGameHost)
				RealGameStartBtn.draw(g);
			CancelBtn.draw(g);
			HomeBtn.draw(g);
			CloseBtn.draw(g);
			for(int i=0;i<2;i++)
				for(int j=0;j<5;j++)
					MoveTeamBtn[i][j].draw(g);
		}
		
		public void update() {
			EpicEngine ee = new EpicEngine();
			
			WaitingRoomInfo renew = gameClient.getRoomInfo();
			this.wri = renew;
			userList1 = renew.getUserList(1);
			userList2 = renew.getUserList(2);
			int originalSize = chatSize;
			int mutex = renew.getChats().size();
			for(int i= originalSize;i<mutex;i++) {
				Chat c = renew.getChats().get(i);
				String str = "";
				if(chatSize != 0)
					ee.appendToPane(chatArea, "\n", new Color(79,79,79));
				if(c.isSystemic()) {
					ee.appendToPane(chatArea, c.getContent(), new Color(79,79,79));
				}else {
					if(c.getSender().equals(Variables.Username))
						ee.appendToPane(chatArea, c.getSender()+": ", new Color(186, 144, 56));
					else
						ee.appendToPane(chatArea, c.getSender()+": ", new Color(145, 136, 129));
					ee.appendToPane(chatArea, c.getContent(), new Color(214, 208, 192));
				}
				if(shouldScroll()) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
						}
					});
				}
				chatSize++;
				
			}
			
			if(gameClient.isUpdated()) {
				for(int j=0;j<2;j++)
					for(int k=0;k<5;k++)
						MoveTeamBtn[j][k].setVisible(false);
				int myTeam = wri.getTeamOfUser(Variables.Username);
				if(myTeam == 1&&userList2.size()<5)
					MoveTeamBtn[1][userList2.size()].setVisible(true);
				else if(myTeam == 2&&userList1.size()<5)
					MoveTeamBtn[0][userList1.size()].setVisible(true);
			}
			
			Variables.ping = gameClient.ping;
			
			if(gameClient.isNextPhaseSignalActiavted()) {
				Starter.pme.exitWaitingPage();
				Starter.pme.GoChampionSelectPage(gameMode, isGameHost, gameServer, gameClient);
			}
		}
		
		public void setThis() {
			Chatr.setFont();
			Map<TextAttribute, Object> attributes = new HashMap<>();

			attributes.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_DEMIBOLD);
			attributes.put(TextAttribute.SIZE, 12);
			chatArea.setFont(Font.getFont(attributes));
			if(this.isGameHost) {
				this.gameServer = new GameServer();
				this.gameServer.setWaitingRoom(new WaitingRoomInfo(this.Roomname, this.password));
				this.gameServer.startServer();
				this.gameClient.connect(new User(Variables.Username, NetworkTag.LOCAL_HOST_ADDRESS, true), NetworkTag.LOCAL_HOST_ADDRESS, this.password);
			}else {
				try {
					this.gameClient.connect(new User(Variables.Username, InetAddress.getLocalHost().getHostAddress(), false), Roomname, this.password);
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		
		
		public WaitingRoom(GameMode mode, String Roomname, String password, boolean isGameHost) {
			this.gameMode = mode;
			this.Roomname = Roomname;
			this.password = password;
			
			this.isGameHost = isGameHost;
			
			Chatr.addEnterListener(new EnterListener() {
				@Override
				public void onEnterKey() {
					// TODO Auto-generated method stub
					if(Chatr.getText().length()==0)return;
					gameClient.sendMessageToServer(NetworkTag.CHAT+"|"+NetworkTag.WAITING_ROOM+"|"+Variables.Username+"|"+Chatr.getText()+"|"+NetworkTag.NON_SYSTEMIC);
					Chatr.setText("");
				}
				
			});

			this.add(Chatr);
			
			Border border = BorderFactory.createMatteBorder(0, 1, 1, 1, new Color(27,37,41,255));
			
			UIManager.getLookAndFeel().uninitialize();
			chatArea.setBounds(new Rectangle(46, 487, 299, 180));
			chatArea.setBackground(new Color(2,11,17,255));
			
			chatArea.setBorder(BorderFactory.createCompoundBorder(border, BorderFactory.createEmptyBorder(5,10,7,10)));
			chatArea.setMargin(new Insets(15,15,15,15));
			chatArea.setEditable(false);
			chatArea.setVisible(true);
			
			scrollPane = new JScrollPane(chatArea);
			scrollPane.setViewportView(chatArea);
			scrollPane.setBackground(new Color(2,11,17,255));
			scrollPane.setBounds(46, 487, 299, 180);
			scrollPane.setBorder(BorderFactory.createEmptyBorder());
			scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(6,0));
			scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
				@Override
				protected void configureScrollBarColors()
	            {
	                this.thumbColor = new Color(102, 84, 36);
	                this.trackColor = new Color(2,11,17);
	                this.thumbDarkShadowColor = new Color(102, 84, 36);
	            }
				@Override
		        protected JButton createDecreaseButton(int orientation) {
		            return createZeroButton();
		        }

		        @Override    
		        protected JButton createIncreaseButton(int orientation) {
		            return createZeroButton();
		        }

		        private JButton createZeroButton() {
		            JButton jbutton = new JButton();
		            jbutton.setPreferredSize(new Dimension(0, 0));
		            jbutton.setMinimumSize(new Dimension(0, 0));
		            jbutton.setMaximumSize(new Dimension(0, 0));
		            return jbutton;
		        }
			});
			scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			scrollPane.setVisible(true);
			
			this.add(scrollPane);
			
			
			setPanelSize(Constants.ClientPanelDimension);
			setVisible(true);
			this.setLayout(null);
			
			
			MoveTeamBtn = new TriggeredButton[2][5];
			for(int i=0;i<2;i++)
				for(int j=0;j<5;j++) {
					MoveTeamBtn[i][j] = new TriggeredButton(
							ImageManager.UnFocusedMoveTeamButtonImage,
							ImageManager.FocusedMoveTeamButtonImage,
							null,
							new Coordinate(365+495*i, 220+50*j),
							new Coordinate(365+495*i, 220+50*j),
							null,
							new Rectangle(365+495*i, 220+50*j, 157, 34),
							400,
							400
							);
					TriggeredButton tb = MoveTeamBtn[i][j];
					MoveTeamBtn[i][j].addOnButtonListener(new onButtonListener() {

						@Override
						public void onClick() {
							// TODO Auto-generated method stub
							
						}

						@Override
						public void onEnter() {
							// TODO Auto-generated method stub
						}

						@Override
						public void onExit() {
							// TODO Auto-generated method stub
							
						}

						@Override
						public void onPress() {
							// TODO Auto-generated method stub
						}

						@Override
						public void onRelease() {
							// TODO Auto-generated method stub
							tb.setEnabled(false);
							ff.playSoundClip(SoundManager.TeamMoveSoundPath, SoundManager.HIGHER_VOLUME);
							gameClient.sendMessageToServer(NetworkTag.MOVE_TEAM_SIGNAL+"|"+Variables.Username);
						}
					});
					
					MoveTeamBtn[i][j].setVisible(false);
					this.add(MoveTeamBtn[i][j]);
				}
			RealGameStartBtn = new TriggeredButton(
					null,
					ImageManager.RealGameStartButtonImage,
					null,
					null,
					new Coordinate(453,665),
					null,
					new Rectangle(461,669,165,32),
					20,
					20
					);
			RealGameStartBtn.addOnButtonListener(new onButtonListener() {

				@Override
				public void onClick() {
					// TODO Auto-generated method stub
					ff.playSoundClip(SoundManager.PressedRealGameStartButtonSoundPath, SoundManager.HIGHER_VOLUME);
					gameClient.sendMessageToServer(NetworkTag.SELECT_START);
				}

				@Override
				public void onEnter() {
					// TODO Auto-generated method stub
					ff.playSoundClip(SoundManager.ActivatedRealGameStartButtonSoundPath, SoundManager.HIGHER_VOLUME);
				}

				@Override
				public void onExit() {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onPress() {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onRelease() {
					// TODO Auto-generated method stub
					
				}
				
			});
			if(isGameHost)
				this.add(RealGameStartBtn);
			CancelBtn = new TriggeredButton(
					null,
					ImageManager.FocusedGameSelectionCancelButtonImage,
					null,
					null,
					new Coordinate(430,669),
					null,
					new Rectangle(428,669,32,32),
					20,
					20
					);
			CancelBtn.addOnButtonListener(new onButtonListener() {
				@Override
				public void onClick() {
					// TODO Auto-generated method stub
					if(isGameHost)
						gameServer.endServer();
					Starter.pme.exitWaitingPage();
					Starter.pme.goClientPage();
					ff.playSoundClip(SoundManager.GameSelectionCancelSoundPath, SoundManager.HIGHER_VOLUME);
				}

				@Override
				public void onEnter() {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onExit() {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onPress() {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onRelease() {
					// TODO Auto-generated method stub
					
				}
			});
			this.add(CancelBtn);
			CloseBtn = new TriggeredButton(
					null,
					ImageManager.FocusedTerminateButtonImage,
					null,
					null,
					new Coordinate(1252,8),
					null,
					new Rectangle(1252,7,14,14),
					0,
					0
					);
			CloseBtn.addOnButtonListener(new onButtonListener() {
				@Override
				public void onClick() {
					if(isGameHost)
						gameServer.endServer();
					ff.playSoundClip(SoundManager.lightClickSoundFilePath, SoundManager.LIGHT_CLICK_SOUND_VOLUME);
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.exit(0);
				}

				@Override
				public void onEnter() {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onExit() {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onPress() {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onRelease() {
					// TODO Auto-generated method stub
					
				}
			});
			this.add(CloseBtn);
			HomeBtn = new TriggeredButton(
					null,
					ImageManager.FocusedHomeButtonImage,
					null,
					null,
					new Coordinate(249,0),
					null,
					new Rectangle(253,2,50,78),
					50,
					400
					);
			HomeBtn.addOnButtonListener(new onButtonListener() {
						@Override
						public void onClick() {
							if(isGameHost)
								gameServer.endServer();
							ff.playSoundClip(SoundManager.lightClickSoundFilePath, SoundManager.LIGHT_CLICK_SOUND_VOLUME);
							Starter.pme.exitWaitingPage();
							Starter.pme.goClientPage();
						}

						@Override
						public void onEnter() {
							// TODO Auto-generated method stub
							
						}

						@Override
						public void onExit() {
							// TODO Auto-generated method stub
							
						}

						@Override
						public void onPress() {
							// TODO Auto-generated method stub
							
						}

						@Override
						public void onRelease() {
							// TODO Auto-generated method stub
							
						}
					});
			this.add(HomeBtn);
			this.addMouseMotionListener(new MouseMotionListener() {

				@Override
				public void mouseDragged(MouseEvent e) {
					// TODO Auto-generated method stub
					//Variables.mousePos.setPos(e.getX(), e.getY());
					if(SwingUtilities.isLeftMouseButton(e)) {
						if(Variables.mousePos.getY()>80)return;
						Dimension frameSize = Starter.frame.getSize();
					    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
					    Point loc = Starter.frame.getLocation();
					    Point diff = new Point(e.getX() - Variables.mousePos.getX(), e.getY() - Variables.mousePos.getY());
						Starter.frame.setLocation((int)(loc.getX()+diff.getX()), (int)(loc.getY()+diff.getY()));
					}
				}

				@Override
				public void mouseMoved(MouseEvent e) {
					// TODO Auto-generated method stub
					Variables.mousePos.setPos(e.getX(), e.getY());
				}
				
			});
		}
		
		public boolean shouldScroll() {
            int minimumValue = scrollPane.getVerticalScrollBar().getValue() + scrollPane.getVerticalScrollBar().getVisibleAmount();
            int maximumValue = scrollPane.getVerticalScrollBar().getMaximum();
            return maximumValue == minimumValue;
		}
		
		public void setPanelSize(Dimension ps) {
			this.setSize(ps);
			this.PanelSize = ps;
		}
}
