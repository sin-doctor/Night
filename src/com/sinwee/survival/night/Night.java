package com.sinwee.survival.night;

import static java.awt.SystemColor.text;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Iterator;

public class Night extends JFrame {
	private static final int WIDTH = 800;
	private static final int HEIGHT = 600;
	private static final int PLAYER_SIZE = 30;
	private static final int ENEMY_SIZE = 20;
	private static final int BULLET_SIZE = 5;
	private static final int SKILL_COOLDOWN = 10000; // 10 seconds in milliseconds
	private static final int SKILL_UI_SIZE = 50;
	private static final int SKILL_UI_MARGIN = 10;
	private boolean showTutorial = true;

	private GamePanel gamePanel;
	private Timer timer;
	private Player player;
	private List<Enemy> enemies;
	private List<Bullet> bullets;
	private Random random;
	private int time;
	private Skill circularShot;

	// 키 상태를 추적하기 위한 변수들
	private boolean leftPressed = false;
	private boolean rightPressed = false;
	private boolean upPressed = false;
	private boolean downPressed = false;

	public Night() {
		setTitle("Sinwee_NightMode_Prototype");
		setSize(WIDTH, HEIGHT);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		setLocationRelativeTo(null);

		player = new Player(WIDTH / 2, HEIGHT / 2, 100, 10);
		enemies = new ArrayList<>();
		bullets = new ArrayList<>();
		random = new Random();
		time = 0;
		circularShot = new Skill(SKILL_COOLDOWN);

		gamePanel = new GamePanel();
		add(gamePanel);

		addKeyListener(new GameKeyListener());
		addMouseListener(new GameMouseListener());
		setFocusable(true);

		timer = new Timer(16, new GameLoop());
	}

	private class GamePanel extends JPanel {
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, getWidth(), getHeight());

			if (showTutorial) {
				// 튜토리얼 화면 그리기
				g.setColor(Color.WHITE);
//				g.setFont(new Font("맑은 고딕", Font.BOLD, 40));
				g.setFont(new Font(Font.DIALOG, Font.BOLD, 40));
				drawCenteredString(g, "밤이 되었습니다", getHeight() / 4 - 20);

				// 점 그리기
				FontMetrics metrics = g.getFontMetrics();
				int textWidth = metrics.stringWidth(String.valueOf(text));
				int startX = (getWidth() - textWidth) / 2 + textWidth - 150;  // 텍스트 끝나는 지점 + 여백
				int y = getHeight() / 4 - 20;

				int dotSize = 6;
				int dotGap = 10;
				for (int i = 0; i < 3; i++) {
					g.fillOval(startX + (dotGap + dotSize) * i, y - dotSize/2, dotSize, dotSize);
				}

				drawCenteredString(g, "최선을 다해 5분 동안 살아남으세요!", getHeight() / 4 + 20);

				g.setFont(new Font("Arial", Font.PLAIN, 20));
				drawCenteredString(g, "[ 조작 방법 ]", getHeight() / 2 - 80);
				drawCenteredString(g, "이동: W A S D", getHeight() / 2 - 40);
				drawCenteredString(g, "공격: 마우스 클릭", getHeight() / 2);
				drawCenteredString(g, "스킬: 1번 키 (데스페라도)", getHeight() / 2 + 40);

				g.setFont(new Font("Arial", Font.BOLD, 24));
				drawCenteredString(g, "클릭하면 게임이 시작됩니다", getHeight() / 2 + 120);
			} else {
				// 플레이어 그리기
				g.setColor(Color.BLUE);
				g.fillOval(player.x, player.y, PLAYER_SIZE, PLAYER_SIZE);
				g.fillRect(player.x + 5, player.y + PLAYER_SIZE, PLAYER_SIZE - 10, PLAYER_SIZE);
				g.drawLine(player.x - 10, player.y + PLAYER_SIZE + 10, player.x + PLAYER_SIZE + 10,
					player.y + PLAYER_SIZE + 10);
				g.drawLine(player.x + PLAYER_SIZE / 2, player.y + PLAYER_SIZE * 2,
					player.x + PLAYER_SIZE / 2 + 15, player.y + PLAYER_SIZE * 2 + 15);
				g.drawLine(player.x + PLAYER_SIZE / 2, player.y + PLAYER_SIZE * 2,
					player.x + PLAYER_SIZE / 2 - 15, player.y + PLAYER_SIZE * 2 + 15);

				// 적 그리기
				g.setColor(Color.RED);
				for (Enemy enemy : enemies) {
					g.fillArc(enemy.x, enemy.y, ENEMY_SIZE, ENEMY_SIZE, 0, 180);
					g.fillRect(enemy.x, enemy.y + ENEMY_SIZE / 2, ENEMY_SIZE, ENEMY_SIZE / 2);
					g.setColor(Color.BLACK);
					g.fillOval(enemy.x + ENEMY_SIZE / 4, enemy.y + ENEMY_SIZE / 4, ENEMY_SIZE / 6,
						ENEMY_SIZE / 6);
					g.fillOval(enemy.x + ENEMY_SIZE * 3 / 4 - ENEMY_SIZE / 6, enemy.y + ENEMY_SIZE / 4,
						ENEMY_SIZE / 6, ENEMY_SIZE / 6);
					g.setColor(Color.WHITE);
					g.drawString(enemy.hp + "", enemy.x, enemy.y - 5);
					g.setColor(Color.RED);
				}

				// 총알 그리기
				g.setColor(Color.YELLOW);
				for (Bullet bullet : bullets) {
					g.fillOval(bullet.x, bullet.y, BULLET_SIZE, BULLET_SIZE);
				}

				// 시간과 HP 표시
				g.setColor(Color.WHITE);
				g.setFont(new Font("Arial", Font.PLAIN, 14));
				g.drawString("Time: " + time / 60 + "s", 10, 20);
				g.drawString("HP: " + player.getHp(), 10, 40);
				g.drawString("Level: " + player.getLevel(), 10, 60);
				g.drawString("EXP: " + player.getExp() + "/" + (player.getLevel() * 5), 10, 80);
				g.drawString("Attack: " + player.getAttack(), 10, 100);
				g.drawString("Monsters Killed: " + player.getMonstersKilled(), 10, 120);

				// 스킬 UI 그리기
				drawSkillUI(g);
			}

			// 디버그용 코드
//			g.setColor(Color.YELLOW);
//			g.drawString("Debug: Skill UI should be visible", 10, HEIGHT - 80);
		}

		private void drawCenteredString(Graphics g, String text, int y) {
			FontMetrics metrics = g.getFontMetrics();
			int x = (getWidth() - metrics.stringWidth(text)) / 2;
			g.drawString(text, x, y);
		}

		private void drawSkillUI(Graphics g) {
			int x = SKILL_UI_MARGIN;
			int y = getHeight() - SKILL_UI_SIZE - SKILL_UI_MARGIN;
			int size = SKILL_UI_SIZE;

			// 스킬 배경
			g.setColor(Color.DARK_GRAY);
			g.fillOval(x, y, size, size);

			// 쿨다운 표시
			g.setColor(Color.CYAN);
			int angle = (int) (360 * circularShot.getCooldownProgress());
			g.fillArc(x, y, size, size, 90, -angle);

			// 회오리 모양 아이콘
			g.setColor(Color.WHITE);
			for (int i = 0; i < 3; i++) {
				int startAngle = i * 120;
				g.drawArc(x + 5, y + 5, size - 10, size - 10, startAngle, 90);
			}

			// 테두리
			g.setColor(Color.LIGHT_GRAY);
			g.drawOval(x, y, size, size);

			// 스킬 번호 (작게 표시)
			g.setColor(Color.WHITE);
			g.setFont(new Font("Arial", Font.BOLD, 12));
			g.drawString("1", x + 21, y - 10);

			// 스킬 이름 (가운데에 "난사" 표시)
			g.setColor(Color.BLACK);
			g.setFont(new Font("맑은 고딕", Font.BOLD, 10));
			FontMetrics metrics = g.getFontMetrics();
			String skillName = "데스페라도";
			int textWidth = metrics.stringWidth(skillName);
			int textHeight = metrics.getHeight();
			g.drawString(skillName, x + (size - textWidth) / 2 + 1, y + (size + textHeight) / 2 - 4);

			// 디버그용 코드
//			System.out.println("Skill UI drawn at: " + x + ", " + y);
		}
	}

	private class GameLoop implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			time++;

			// 플레이어 이동 업데이트
			updatePlayerMovement();
			circularShot.update();

			if (random.nextInt(100) < 5) {
				int x = random.nextInt(WIDTH - ENEMY_SIZE);
				int y = random.nextInt(HEIGHT - ENEMY_SIZE);
				enemies.add(new Enemy(x, y, 50, 5));
			}

			for (Enemy enemy : enemies) {
				enemy.moveTowards(player);
				enemy.attack(player);
			}

			// 총알 이동 및 충돌 처리
			Iterator<Bullet> bulletIterator = bullets.iterator();
			while (bulletIterator.hasNext()) {
				Bullet bullet = bulletIterator.next();
				bullet.move();
				if (bullet.isOutOfBounds()) {
					bulletIterator.remove();
					continue;
				}
				for (Enemy enemy : enemies) {
					if (bullet.hitEnemy(enemy)) {
						enemy.takeDamage(player.attack);
						if (!enemy.isAlive()) {
							player.gainExp();
						}
						bulletIterator.remove();
						break;
					}
				}
			}

			enemies.removeIf(enemy -> !enemy.isAlive());

			if (!player.isAlive() || time >= 18000) { // 5분(300초 * 60프레임)
				timer.stop();
				JOptionPane.showMessageDialog(null,
					player.isAlive() ? "밤이 지났습니다!" : "잡혀먹혔습니다!");
				System.exit(0);
			}

			gamePanel.repaint();
		}

		private void updatePlayerMovement() {
			int dx = 0;
			int dy = 0;
			if (leftPressed) dx -= 5;
			if (rightPressed) dx += 5;
			if (upPressed) dy -= 5;
			if (downPressed) dy += 5;
			player.move(dx, dy);
		}
	}

	private class GameKeyListener extends KeyAdapter {
		@Override
		public void keyPressed(KeyEvent e) {
			setKeyState(e.getKeyCode(), true);
			if (e.getKeyCode() == KeyEvent.VK_1) {
				useCircularShot();
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
			setKeyState(e.getKeyCode(), false);
		}

		private void setKeyState(int keyCode, boolean isPressed) {
			switch (keyCode) {
				case KeyEvent.VK_A:
					leftPressed = isPressed;
					break;
				case KeyEvent.VK_D:
					rightPressed = isPressed;
					break;
				case KeyEvent.VK_W:
					upPressed = isPressed;
					break;
				case KeyEvent.VK_S:
					downPressed = isPressed;
					break;
			}
		}
	}

	private void useCircularShot() {
		if (circularShot.use()) {
			for (int i = 0; i < 40; i++) {
				double angle = 2 * Math.PI * i / 40;
				int targetX = (int) (player.x + PLAYER_SIZE / 2 + 100 * Math.cos(angle));
				int targetY = (int) (player.y + PLAYER_SIZE / 2 + 100 * Math.sin(angle));
				bullets.add(new Bullet(player.x + PLAYER_SIZE / 2, player.y + PLAYER_SIZE / 2, targetX, targetY));
			}
		}
	}

	private class GameMouseListener extends MouseAdapter {
		@Override
		public void mousePressed(MouseEvent e) {
			if (showTutorial) {
				showTutorial = false;
				timer.start();  // 게임 시작
				return;
			}

			int mouseX = e.getX();
			int mouseY = e.getY();
			bullets.add(new Bullet(player.x + PLAYER_SIZE / 2, player.y + PLAYER_SIZE / 2, mouseX, mouseY));
		}
	}

	private class Player {
		int x, y;
		private int hp;
		int attack;
		int level;
		int exp;
		int monstersKilled;

		public Player(int x, int y, int hp, int attack) {
			this.x = x;
			this.y = y;
			this.hp = hp;
			this.attack = attack;
			this.level = 1;
			this.exp = 0;
			this.monstersKilled = 0;
		}

		public void move(int dx, int dy) {
			x = Math.max(0, Math.min(WIDTH - PLAYER_SIZE, x + dx));
			y = Math.max(0, Math.min(HEIGHT - PLAYER_SIZE * 2 - 15, y + dy));
		}

		public void levelUp() {
			hp += 20;
			attack += 5;
			level++;
			exp = 0;
		}

		public  void gainExp() {
			exp++;
			monstersKilled++;
			if (exp >= level * 5) {
				levelUp();
			}
		}

		public void takeDamage(int damage) {
			hp -= damage;
		}

		public boolean isAlive() {
			return hp > 0;
		}

		public int getHp() {
			return hp;
		}

		public int getLevel() {
			return level;
		}

		public int getAttack() {
			return attack;
		}

		public int getExp() {
			return exp;
		}

		public int getMonstersKilled() {
			return monstersKilled;
		}
	}

	private class Enemy {
		int x, y;
		int hp;
		private int attack;

		public Enemy(int x, int y, int hp, int attack) {
			this.x = x;
			this.y = y;
			this.hp = hp;
			this.attack = attack;
		}

		public void moveTowards(Player player) {
			int dx = player.x - x;
			int dy = player.y - y;
			double distance = Math.sqrt(dx * dx + dy * dy);
			if (distance > 0) {
				x += (dx / distance) * 2;
				y += (dy / distance) * 2;
			}
		}

		public void attack(Player player) {
			if (Math.sqrt(Math.pow(x - player.x, 2) + Math.pow(y - player.y, 2)) < 30) {
				player.takeDamage(attack);
			}
		}

		public void takeDamage(int damage) {
			hp -= damage;
		}

		public boolean isAlive() {
			return hp > 0;
		}
	}

	private class Bullet {
		int x, y;
		private double dx, dy;
		private static final double SPEED = 10;

		public Bullet(int startX, int startY, int targetX, int targetY) {
			this.x = startX;
			this.y = startY;
			double angle = Math.atan2(targetY - startY, targetX - startX);
			this.dx = Math.cos(angle) * SPEED;
			this.dy = Math.sin(angle) * SPEED;
		}

		public void move() {
			x += dx;
			y += dy;
		}

		public boolean isOutOfBounds() {
			return x < 0 || x > WIDTH || y < 0 || y > HEIGHT;
		}

		public boolean hitEnemy(Enemy enemy) {
			return Math.sqrt(Math.pow(x - enemy.x, 2) + Math.pow(y - enemy.y, 2)) < ENEMY_SIZE;
		}
	}

	private class Skill {
		private long lastUsed;
		private int cooldown;

		public Skill(int cooldown) {
			this.cooldown = cooldown;
			this.lastUsed = -cooldown;
		}

		public boolean use() {
			long currentTime = System.currentTimeMillis();
			if (currentTime - lastUsed >= cooldown) {
				lastUsed = currentTime;
				return true;
			}
			return false;
		}

		public void update() {
			// 필요한 경우 여기에 추가 로직을 구현할 수 있습니다.
		}

		public double getCooldownProgress() {
			long currentTime = System.currentTimeMillis();
			long elapsedTime = currentTime - lastUsed;
			return Math.min(1.0, (double) elapsedTime / cooldown);
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			Night game = new Night();
			game.setVisible(true);
			// 디버그용 코드
//			System.out.println("Game window size: " + game.getWidth() + "x" + game.getHeight());
		});
	}
}