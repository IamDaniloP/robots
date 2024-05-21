package quartetofantastico;

import robocode.*;
import robocode.util.Utils;
import java.awt.geom.*;
import java.lang.*;
import java.util.ArrayList;
import java.awt.Color;

// O Coisa - a class by (Quarteto Fantastico)

public class OCoisa extends AdvancedRobot {
    public static int BINS = 47;
    public static double[] statusSurf = new double[BINS];
    public Point2D.Double minhaLocalizacao;
    public ArrayList direcaoSurf;
    public ArrayList anguloSurf;
	private static final double POTENCIA_PROJETIL = 1.9;
	private static double direcaoLateral;


    public Point2D.Double localizacaoOponente;
    public ArrayList ondasDoOponente;
    private static double ultimaVelocidadeInimigo;
    public static double energiaOponente = 100.0;

    public static Rectangle2D.Double dimensoesArena;

    public void run() {
        setColors(Color.orange, Color.white, Color.orange, Color.black, Color.orange);

        dimensoesArena = new java.awt.geom.Rectangle2D.Double(18, 18, getBattleFieldWidth() - 36, getBattleFieldHeight() - 36);

        direcaoLateral = 1;
        ultimaVelocidadeInimigo = 0;

        ondasDoOponente = new ArrayList();
        direcaoSurf = new ArrayList();
        anguloSurf = new ArrayList();

        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        do {
            turnRadarRightRadians(Double.POSITIVE_INFINITY);
        } while (true);
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        minhaLocalizacao = new Point2D.Double(getX(), getY());

        // calcula a velocidade lateral do oponente
        double velocidadeLateral = getVelocity()*Math.sin(e.getBearingRadians());
        double anguloAbsoluto = e.getBearingRadians() + getHeadingRadians();

        setTurnRadarRightRadians(Utils.normalRelativeAngle(anguloAbsoluto - getRadarHeadingRadians()) * 2);

        direcaoSurf.add(0, (velocidadeLateral >= 0) ? 1 : -1);
        anguloSurf.add(0, anguloAbsoluto + Math.PI);

        // calcula potência do projétil inimigo para saber a velocidade para desviar
        double potenciaProjetil = energiaOponente - e.getEnergy();
        if (potenciaProjetil < 3.01 && potenciaProjetil > 0.09 && direcaoSurf.size() > 2) {
            // cria uma onda do oponente para armazenar
            OndaDoOponente ondaDoOponente = new OndaDoOponente();
            ondaDoOponente.tempoDisparo = getTime() - 1;
            ondaDoOponente.velocidadeProjetil = velocidadeProjetil(potenciaProjetil);
            ondaDoOponente.distanciaPercorrida = velocidadeProjetil(potenciaProjetil);
            ondaDoOponente.direcao = ((Integer)direcaoSurf.get(2));
            ondaDoOponente.anguloDireto = ((Double)anguloSurf.get(2));
            ondaDoOponente.localDisparo = (Point2D.Double)localizacaoOponente.clone();

            // adiciona ao array cada onda do oponente
            ondasDoOponente.add(ondaDoOponente);
        }

        // atualiza a energia do oponente após o disparo
        energiaOponente = e.getEnergy();

        // projeta a localização para realizar o desvio
        localizacaoOponente = projetarMov(minhaLocalizacao, anguloAbsoluto, e.getDistance());

        atualizarOnda();
        realizarSurfing();

		double anguloAbsolutoOponente = getHeadingRadians() + e.getBearingRadians();
		double distanciaOponente = e.getDistance();
		double velocidadeOponente = e.getVelocity();
		if (velocidadeOponente != 0) {
            direcaoLateral = UtilitariosGFT.sinal(velocidadeOponente * Math.sin(e.getHeadingRadians() - anguloAbsolutoOponente));
		}

        // cria uma onda para calcular a segmentação e realizar o disparo
		OndaGFT ondaGFT = new OndaGFT(this);
        ondaGFT.localArma = new Point2D.Double(getX(), getY());
		OndaGFT.localAlvo = UtilitariosGFT.projetarMov(ondaGFT.localArma, anguloAbsolutoOponente, distanciaOponente);
        ondaGFT.direcaoLateral = direcaoLateral;
        ondaGFT.potenciaProjetil = POTENCIA_PROJETIL;
        ondaGFT.setSubdivisoes(distanciaOponente, velocidadeOponente, ultimaVelocidadeInimigo);
        ultimaVelocidadeInimigo = velocidadeOponente;
        ondaGFT.angulo = anguloAbsolutoOponente;

        // depois de realizar a estimativa, ele configura a direção do canhão e realiza o disparo
        setTurnGunRightRadians(Utils.normalRelativeAngle(anguloAbsolutoOponente - getGunHeadingRadians() + ondaGFT.deslocamentoDoAnguloMaisVisitado()));
		setFire(ondaGFT.potenciaProjetil);

        // adicionar a onda GFT ao sistema de eventos customizados
        if (getEnergy() >= POTENCIA_PROJETIL) {
			addCustomEvent(ondaGFT);
		}

        // ajustar a direção do radar novamente
		setTurnRadarRightRadians(Utils.normalRelativeAngle(anguloAbsolutoOponente - getRadarHeadingRadians()) * 2);

        /*
         * A ideia para a implementação da regressão logística é evitar perder o alcance para o robô adversário.
         * Seguindo as estratégias adotadas que foram: WaveSurfer e Guess Factor Target, o robô só conseguiria
         * desempenhar seu papel em arenas de até no máximo 800x600. Porém, devido à necessidade de uma arena de até 1200x1200,
         * resolvi adotar essas mesmas estratégias e adaptá-las ao meu robô.
         *
         * O método de classificação escolhido para implementar essa funcionalidade no robô foi a regressão logística binária.
         * Para isso, utilizamos dois parâmetros: distância para o robô adversário e a velocidade do meu robô.
         * A cada vez que o robô adversário for escaneado, ele fará essa verificação no if abaixo.
         *
         * Caso o coeficiente seja menor que 0,5, o resultado será "0", indicando que o robô adversário não saiu
         * da área do scanner do meu robô. Se o coeficiente for igual ou maior que 0,5, o resultado será "1", indicando que
         * o robô adversário saiu da área do scanner. Quando o coeficiente é "1" (ou seja, maior ou igual a 0,5), ele apontará o chassi
         * do tanque para a última posição em que o canhão estava, aumentando a probabilidade de encontrar o robô inimigo,
         * considerando que sua velocidade máxima é de 8.0 pixels/segundo.
         *
         * Não configurei o método getCoeficiente para retornar apenas 0 e 1, pois queria tornar os coeficientes visíveis.
         * Optei por fazer a verificação diretamente dentro do if.
         */
        double minhaDistancia = e.getDistance();
        double minhaVelocidade = getVelocity();
        if (RegressaoLogisticaBinaria.getCoeficiente(minhaDistancia, minhaVelocidade) >= 0.5) {
            System.out.println("Distância: " + minhaDistancia);
            System.out.println("Velocidade: " + minhaVelocidade);
            System.out.println("Coeficiente da Regressão Logística: " + RegressaoLogisticaBinaria.getCoeficiente(minhaDistancia, minhaVelocidade));
            System.out.println("Perdeu");

            double direcaoCanhao = Utils.normalRelativeAngle(getGunHeadingRadians() - getHeadingRadians());
            setTurnRightRadians(direcaoCanhao);
            setAhead(500); // não tem problema o valor alto, pois assim que o oponente for encontrado ele não entra no if
        }
    }

    // atualiza a distância percorrida pelas ondas do oponente e remove as que passaram
    public void atualizarOnda() {
        for (int x = 0; x < ondasDoOponente.size(); x++) {
            OndaDoOponente ondaDoOponente = (OndaDoOponente)ondasDoOponente.get(x);

            ondaDoOponente.distanciaPercorrida = (getTime() - ondaDoOponente.tempoDisparo) * ondaDoOponente.velocidadeProjetil;
            if (ondaDoOponente.distanciaPercorrida > minhaLocalizacao.distance(ondaDoOponente.localDisparo) + 50) {
                ondasDoOponente.remove(x);
                x--;
            }
        }
    }

    // obtém a onda do oponente mais próxima
    public OndaDoOponente getOndaProxima() {
        double distanciaMaisProxima = 10000;
        OndaDoOponente ondaProxima = null;

        for (Object enemyWave : ondasDoOponente) {
            OndaDoOponente ondaDoOponente = (OndaDoOponente) enemyWave;
            double distance = minhaLocalizacao.distance(ondaDoOponente.localDisparo) - ondaDoOponente.distanciaPercorrida;

            if (distance > ondaDoOponente.velocidadeProjetil && distance < distanciaMaisProxima) {
                ondaProxima = ondaDoOponente;
                distanciaMaisProxima = distance;
            }
        }

        return ondaProxima;
    }

    // calcula o índice do fator baseado no ângulo de desvio e no máximo ângulo de escape
    public static int getIndiceFator(OndaDoOponente ondaDoOponente, Point2D.Double localizacaoOponente) {
        double anguloDesvio = (anguloAbsoluto(ondaDoOponente.localDisparo, localizacaoOponente) - ondaDoOponente.anguloDireto);
        double fator = Utils.normalRelativeAngle(anguloDesvio) / maximoAnguloEscape(ondaDoOponente.velocidadeProjetil) * ondaDoOponente.direcao;

        return (int)limite(0, (fator * ((double) (BINS - 1) / 2)) + ((double) (BINS - 1) / 2), BINS - 1);
    }

    // registra um acerto no array de status de surf
    public void registraAcerto(OndaDoOponente ondaDoOponente, Point2D.Double localizacaoOponente) {
        int index = getIndiceFator(ondaDoOponente, localizacaoOponente);

        for (int x = 0; x < BINS; x++) {
            statusSurf[x] += 1.0 / (Math.pow(index - x, 2) + 1);
        }
    }

    // registra quando o robô é atingido por um projétil
    public void onHitByBullet(HitByBulletEvent e) {
        if (!ondasDoOponente.isEmpty()) {
            Point2D.Double localizacaoMomentoAtingido = new Point2D.Double(e.getBullet().getX(), e.getBullet().getY());
            OndaDoOponente ondaMomentoAtingido = null;

            for (Object ondaOponente : ondasDoOponente) {
                OndaDoOponente ondaDoOponente = (OndaDoOponente) ondaOponente;

                if (Math.abs(ondaDoOponente.distanciaPercorrida - minhaLocalizacao.distance(ondaDoOponente.localDisparo)) < 50 && Math.abs(velocidadeProjetil(e.getBullet().getPower()) - ondaDoOponente.velocidadeProjetil) < 0.001) {
                    ondaMomentoAtingido = ondaDoOponente;
                    break;
                }
            }

            if (ondaMomentoAtingido != null) {
                registraAcerto(ondaMomentoAtingido, localizacaoMomentoAtingido);
                ondasDoOponente.remove(ondasDoOponente.lastIndexOf(ondaMomentoAtingido));
            }
        }
    }

    // calcula a posição prevista do robô para evitar tiros
    public Point2D.Double posicaoPrevista(OndaDoOponente ondaDoOponente, int direcao) {
    	Point2D.Double posicaoPrevista = (Point2D.Double)minhaLocalizacao.clone();
    	double velocidadePrevista = getVelocity();
    	double direcaoPrevista = getHeadingRadians();
    	double viradaMaxima;
        double anguloMovimento;
        double direcaoMovimento;

        int contador = 0;
        boolean interceptado = false;

    	do {
            anguloMovimento = evitaParede(posicaoPrevista, anguloAbsoluto(ondaDoOponente.localDisparo, posicaoPrevista) + (direcao * (Math.PI/2)), direcao) - direcaoPrevista;
            direcaoMovimento = 1;

    		if(Math.cos(anguloMovimento) < 0) {
                anguloMovimento += Math.PI;
                direcaoMovimento = -1;
    		}

            anguloMovimento = Utils.normalRelativeAngle(anguloMovimento);

            viradaMaxima = Math.PI/720d*(40d - 3d*Math.abs(velocidadePrevista));
            direcaoPrevista = Utils.normalRelativeAngle(direcaoPrevista + limite(-viradaMaxima, anguloMovimento, viradaMaxima));

            velocidadePrevista += (velocidadePrevista * direcaoMovimento < 0 ? 2*direcaoMovimento : direcaoMovimento);
            velocidadePrevista = limite(-8, velocidadePrevista, 8);

            posicaoPrevista = projetarMov(posicaoPrevista, direcaoPrevista, velocidadePrevista);

            contador++;

            if (posicaoPrevista.distance(ondaDoOponente.localDisparo) < ondaDoOponente.distanciaPercorrida + (contador * ondaDoOponente.velocidadeProjetil) + ondaDoOponente.velocidadeProjetil) {
                interceptado = true;
            }
    	} while(!interceptado && contador < 500);

    	return posicaoPrevista;
    }

    // calcula o perigo baseado no índice do fator
    public double checarPerigo(OndaDoOponente ondaDoOponente, int direcao) {
        int index = getIndiceFator(ondaDoOponente,
                posicaoPrevista(ondaDoOponente, direcao));

        return statusSurf[index];
    }

    // realiza o movimento de surf para evitar tiros
    public void realizarSurfing() {
        OndaDoOponente ondaDoOponente = getOndaProxima();

        if (ondaDoOponente == null) { return; }

        double perigoEsquerda = checarPerigo(ondaDoOponente, -1);
        double perigoDireita = checarPerigo(ondaDoOponente, 1);

        double anguloDesvio = anguloAbsoluto(ondaDoOponente.localDisparo, minhaLocalizacao);
        if (perigoEsquerda < perigoDireita) {
            anguloDesvio = evitaParede(minhaLocalizacao, anguloDesvio - (Math.PI/2), -1);
        } else {
            anguloDesvio = evitaParede(minhaLocalizacao, anguloDesvio + (Math.PI/2), 1);
        }

        tornarTrazeiraParaDianteira(this, anguloDesvio);
    }

    // evita que o robô colida com as paredes
    public double evitaParede(Point2D.Double localizacaoRobo, double angulo, int orientacao) {
        while (!dimensoesArena.contains(projetarMov(localizacaoRobo, angulo, 160))) {
            angulo += orientacao*0.05;
        }
        return angulo;
    }

    // projeta o movimento baseado no ângulo e comprimento
    public static Point2D.Double projetarMov(Point2D.Double localOrigem, double angulo, double comprimento) {
        return new Point2D.Double(localOrigem.x + Math.sin(angulo) * comprimento,
                localOrigem.y + Math.cos(angulo) * comprimento);
    }

    // calcula o ângulo absoluto entre dois pontos
    public static double anguloAbsoluto(Point2D.Double source, Point2D.Double target) {
        return Math.atan2(target.x - source.x, target.y - source.y);
    }

    // limita um valor a um intervalo específico
    public static double limite(double min, double valor, double max) {
        return Math.max(min, Math.min(valor, max));
    }

    // calcula a velocidade do projétil baseado na potência
    public static double velocidadeProjetil(double potencia) {
        return (20D - (3D*potencia));
    }

    // calcula o máximo ângulo de escape baseado na velocidade
    public static double maximoAnguloEscape(double velocidade) {
        return Math.asin(8.0/velocidade);
    }

    // converte o ângulo traseiro para dianteiro e movimenta o robô
    public static void tornarTrazeiraParaDianteira(AdvancedRobot robo, double anguloDesvio) {
        double angulo = Utils.normalRelativeAngle(anguloDesvio - robo.getHeadingRadians());
        if (Math.abs(angulo) > (Math.PI/2)) {
            if (angulo < 0) {
                robo.setTurnRightRadians(Math.PI + angulo);
            } else {
                robo.setTurnLeftRadians(Math.PI - angulo);
            }
            robo.setBack(100);
        } else {
            if (angulo < 0) {
                robo.setTurnLeftRadians(-1*angulo);
           } else {
                robo.setTurnRightRadians(angulo);
           }
            robo.setAhead(100);
        }
    }
}
