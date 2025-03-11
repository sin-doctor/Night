# Day & Night: Survival Tycoon

## 소개
**Day & Night: Survival Tycoon**는 **낮에는 농사 및 자원 채집으로 성장하고, 밤에는 몰려오는 적들로부터 5분간 살아남아야 하는 서바이벌 게임**입니다.  
이 게임은 **타이쿤(Tycoon) 요소**와 **뱀파이어 서바이벌(Vampire Survivor) 요소**를 결합하여 설계되었습니다.

### 게임 특징
- **낮에는 자원 채집과 성장**
- **밤에는 사방에서 몰려오는 적들과 전투**
- **5분 동안 생존하면 승리**
- **일정 수의 적을 처치하면 스킬 사용 가능**
- **마우스와 `WASD` 키를 사용한 간편한 조작 방식**

---

## 동작 방식

### 1. 기본 시스템
- **낮에는 플레이어가 자원을 모으고 성장하는 단계입니다.**
- **밤이 되면 사방에서 적이 몰려오며, 플레이어는 살아남아야 합니다.**
- **5분 동안 생존하면 승리하며, 플레이어가 사망하면 게임 오버입니다.**

### 2. 전투 시스템
- 마우스로 공격할 방향을 지정하고 클릭하면 총알을 발사합니다.
- 적을 일정 수 처치하면 `1번 키`로 스킬(데스페라도)을 사용할 수 있습니다.
- 적들은 플레이어를 향해 이동하며, 근접 공격을 가합니다.

### 3. 스킬 시스템
- **데스페라도 (Desperado)**:  
  - 주변의 적을 일시에 공격하는 강력한 광역 스킬  
  - 일정 시간(10초) 동안 재사용 불가능 (`쿨다운 시스템` 적용)  

### 4. 조작 방법
| 조작키 | 기능 |
|--------|------|
| `W` `A` `S` `D` | 플레이어 이동 |
| `마우스 클릭` | 공격(총알 발사) |
| `1번 키` | 스킬(데스페라도) 사용 |

---

## 실행 방법

### 1. Java 실행 환경 확인
본 게임은 **Java 23+** 환경에서 실행됩니다.  
다음 명령어로 설치된 Java 버전을 확인하세요.

```sh
java -version
```

### 2. 게임 실행
아래 방법 중 하나를 선택하여 게임을 실행할 수 있습니다.

#### (1) `javac` 및 `java` 명령어를 사용하여 실행
```sh
javac -d . Night.java
java com.sinwee.survival.night.Night
```

#### (2) IDE (IntelliJ, Eclipse 등)에서 실행
1. `Night.java` 파일을 엽니다.
2. 실행 버튼 (`▶`)을 눌러 실행합니다.

---

## 코드 설명

### 1. `Night` 클래스 (메인 게임)
```java
public class Night extends JFrame {
    private GamePanel gamePanel;
    private Timer timer;
    private Player player;
    private List<Enemy> enemies;
    private List<Bullet> bullets;
}
```
- **`JFrame`을 상속받아 게임 창을 생성**합니다.
- **게임 패널(`GamePanel`)을 추가**하여 그래픽을 처리합니다.
- **타이머(`Timer`)를 활용하여 16ms(약 60FPS)마다 게임을 업데이트**합니다.

---

### 2. `Player` 클래스 (플레이어)
```java
private class Player {
    int x, y;
    private int hp;
    int attack;
    int level;
    int exp;
    int monstersKilled;
}
```
- 플레이어의 위치, 체력(`hp`), 공격력(`attack`), 레벨(`level`), 경험치(`exp`) 등을 저장합니다.
- 적을 처치하면 경험치를 획득하며, 경험치가 일정량 이상이면 **레벨업**합니다.
- 레벨업 시 **체력 증가, 공격력 증가** 등의 효과가 적용됩니다.

---

### 3. `Enemy` 클래스 (적)
```java
private class Enemy {
    int x, y;
    int hp;
    private int attack;
    
    public void moveTowards(Player player) {
        int dx = player.x - x;
        int dy = player.y - y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        if (distance > 0) {
            x += (dx / distance) * 2;
            y += (dy / distance) * 2;
        }
    }
}
```
- 적은 **플레이어를 향해 자동으로 이동**하며 근접 공격을 가합니다.
- 체력이 0이 되면 사라지며, 처치 시 플레이어가 경험치를 얻습니다.

---

### 4. `Bullet` 클래스 (총알)
```java
private class Bullet {
    int x, y;
    private double dx, dy;
    
    public void move() {
        x += dx;
        y += dy;
    }

    public boolean hitEnemy(Enemy enemy) {
        return Math.sqrt(Math.pow(x - enemy.x, 2) + Math.pow(y - enemy.y, 2)) < ENEMY_SIZE;
    }
}
```
- 플레이어가 마우스로 클릭하면 총알을 발사합니다.
- 총알은 특정 방향으로 이동하며, 적과 충돌 시 공격을 가합니다.

---

### 5. `Skill` 클래스 (스킬)
```java
private class Skill {
    private long lastUsed;
    private int cooldown;

    public boolean use() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUsed >= cooldown) {
            lastUsed = currentTime;
            return true;
        }
        return false;
    }
}
```
- **데스페라도(Desperado) 스킬을 위한 클래스**입니다.
- **쿨다운 시스템**을 적용하여 일정 시간(10초) 동안 재사용할 수 없습니다.

---

### 6. `GameLoop` 클래스 (게임 루프)
```java
private class GameLoop implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        time++;
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

        if (!player.isAlive() || time >= 18000) {
            timer.stop();
            JOptionPane.showMessageDialog(null,
                player.isAlive() ? "밤이 지났습니다!" : "잡혀먹혔습니다!");
            System.exit(0);
        }

        gamePanel.repaint();
    }
}
```
- 게임이 진행되는 동안 **적을 생성하고 이동하며, 플레이어와 충돌 시 공격하는 역할**을 합니다.
- **5분이 지나면 승리 메시지, 체력이 0이 되면 패배 메시지를 출력**합니다.

---

## 향후 개선 사항
- 낮 시간 추가 (농사 및 자원 채집 시스템 도입)
- 다양한 적 유형 추가 (원거리 적, 보스 등)
- 새로운 스킬 추가 (자신을 보호하는 방어막 등)
- 멀티플레이어 기능 추가

---

## 라이선스
이 프로젝트는 개인 프로젝트로 개발되었으며, 자유롭게 활용할 수 있습니다.  
단, 코드 사용 시 출처를 표기해 주세요.
