import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

// 메인 클래스 - 전체 흐름을 관리
public class TowerDefenseGame extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private GamePanel gamePanel;
    private ShopPanel shopPanel;
    private SettingsPanel settingsPanel;
    private StageSelectPanel stageSelectPanel;

    public TowerDefenseGame() {
        setTitle("Tower Defense Game");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // 게임 패널 초기화
        gamePanel = new GamePanel();
        shopPanel = new ShopPanel(gamePanel);
        settingsPanel = new SettingsPanel(gamePanel);
        stageSelectPanel = new StageSelectPanel(gamePanel);

        // 메인 메뉴 패널
        JPanel mainMenuPanel = new JPanel();
        mainMenuPanel.setLayout(new BoxLayout(mainMenuPanel, BoxLayout.Y_AXIS));

        JButton startGameButton = new JButton("게임 시작");
        JButton shopButton = new JButton("상점");
        JButton settingsButton = new JButton("설정");
        JButton stageSelectButton = new JButton("스테이지 선택");

        startGameButton.addActionListener(e -> cardLayout.show(mainPanel, "Game"));
        shopButton.addActionListener(e -> cardLayout.show(mainPanel, "Shop"));
        settingsButton.addActionListener(e -> cardLayout.show(mainPanel, "Settings"));
        stageSelectButton.addActionListener(e -> cardLayout.show(mainPanel, "StageSelect"));

        mainMenuPanel.add(startGameButton);
        mainMenuPanel.add(shopButton);
        mainMenuPanel.add(settingsButton);
        mainMenuPanel.add(stageSelectButton);

        // 패널 추가
        mainPanel.add(mainMenuPanel, "MainMenu");
        mainPanel.add(gamePanel, "Game");
        mainPanel.add(shopPanel, "Shop");
        mainPanel.add(settingsPanel, "Settings");
        mainPanel.add(stageSelectPanel, "StageSelect");

        add(mainPanel);
        cardLayout.show(mainPanel, "MainMenu"); // 시작은 메인 메뉴
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TowerDefenseGame game = new TowerDefenseGame();
            game.setVisible(true);
        });
    }
}

// 게임 로직을 처리하는 패널
class GamePanel extends JPanel implements ActionListener {
    private Timer gameTimer;
    private int gold;
    private double resourceMultiplier = 1.0;
    private int resourceUpgradeLevel = 0;
    private int baseHealth = 1000;

    public GamePanel() {
        gold = 1000; // 초기 골드 설정
        gameTimer = new Timer(1000 / 60, this); // 60 FPS
        gameTimer.start();
    }

    // 자원 획득 속도 업그레이드
    public void upgradeResourceProduction() {
        int cost = 200 + resourceUpgradeLevel * 50;
        if (gold >= cost) {
            gold -= cost;
            resourceUpgradeLevel++;
            resourceMultiplier += 0.2;
        }
    }

    // 스테이지 설정
    public void setStage(int stage) {
        // 스테이지에 따른 난이도 변경 처리
        System.out.println("스테이지: " + stage);
    }

    // 배속 설정
    public void setGameSpeed(double speed) {
        gameTimer.setDelay((int) (1000 / 60 / speed));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // 게임 로직 업데이트
        updateGame();
        repaint();
    }

    private void updateGame() {
        // 유닛과 적 전투 및 타워 방어 로직 처리
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // 게임 화면 그리기
        g.drawString("Gold: " + gold, 10, 20);
    }
}

// 상점 패널 - 자원 업그레이드
class ShopPanel extends JPanel {
    public ShopPanel(GamePanel gamePanel) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JButton upgradeResourceButton = new JButton("자원 생산 속도 업그레이드");
        upgradeResourceButton.addActionListener(e -> gamePanel.upgradeResourceProduction());
        add(upgradeResourceButton);

        JButton backButton = new JButton("뒤로");
        backButton.addActionListener(e -> {
            CardLayout cl = (CardLayout) getParent().getLayout();
            cl.show(getParent(), "MainMenu");
        });
        add(backButton);
    }
}

// 설정 패널 - 게임 배속 조정
class SettingsPanel extends JPanel {
    public SettingsPanel(GamePanel gamePanel) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JButton speed1_2Button = new JButton("1.2배속");
        speed1_2Button.addActionListener(e -> gamePanel.setGameSpeed(1.2));
        add(speed1_2Button);

        JButton speed1_5Button = new JButton("1.5배속");
        speed1_5Button.addActionListener(e -> gamePanel.setGameSpeed(1.5));
        add(speed1_5Button);

        JButton backButton = new JButton("뒤로");
        backButton.addActionListener(e -> {
            CardLayout cl = (CardLayout) getParent().getLayout();
            cl.show(getParent(), "MainMenu");
        });
        add(backButton);
    }
}

// 스테이지 선택 패널
class StageSelectPanel extends JPanel {
    public StageSelectPanel(GamePanel gamePanel) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JButton stage1Button = new JButton("스테이지 1");
        stage1Button.addActionListener(e -> {
            gamePanel.setStage(1);
            CardLayout cl = (CardLayout) getParent().getLayout();
            cl.show(getParent(), "Game");
        });
        add(stage1Button);

        JButton stage2Button = new JButton("스테이지 2");
        stage2Button.addActionListener(e -> {
            gamePanel.setStage(2);
            CardLayout cl = (CardLayout) getParent().getLayout();
            cl.show(getParent(), "Game");
        });
        add(stage2Button);

        JButton backButton = new JButton("뒤로");
        backButton.addActionListener(e -> {
            CardLayout cl = (CardLayout) getParent().getLayout();
            cl.show(getParent(), "MainMenu");
        });
        add(backButton);
    }
}
