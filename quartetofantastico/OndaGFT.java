package quartetofantastico;

import robocode.AdvancedRobot;
import robocode.Condition;
import robocode.util.Utils;

import java.awt.geom.Point2D;

public class OndaGFT extends Condition {
    static Point2D localAlvo;

    double potenciaProjetil;
    Point2D localArma;
    double angulo;
    double direcaoLateral;

    private static final double DISTANCIA_MAXIMA = 900;
    private static final int INDICES_DISTANCIA = 5;
    private static final int INDICES_VELOCIDADE = 5;
    private static final int BINS = 25;
    private static final int BIN_MEIO = (BINS - 1) / 2;
    private static final double ANGULO_DE_MAXIMO_ESCAPE = 0.7;
    private static final double LARGURA_BIN = ANGULO_DE_MAXIMO_ESCAPE / (double)BIN_MEIO;

    private static int[][][][] buffersEstatisticas = new int[INDICES_DISTANCIA][INDICES_VELOCIDADE][INDICES_VELOCIDADE][BINS];

    private int[] buffer;
    private AdvancedRobot robo;
    private double distanciaPercorrida;

    OndaGFT(AdvancedRobot robo) {
        this.robo = robo;
    }

    public boolean test() {
        avancar();
        if (jaChegou()) {
            buffer[binAtual()]++;
            robo.removeCustomEvent(this);
        }
        return false;
    }

    double deslocamentoDoAnguloMaisVisitado() {
        return (direcaoLateral * LARGURA_BIN) * (binMaisVisitado() - BIN_MEIO);
    }

    void setSubdivisoes(double distancia, double velocidade, double ultimaVelocidade) {
        int indiceDistancia = Math.min(INDICES_DISTANCIA-1, (int)(distancia / (DISTANCIA_MAXIMA / INDICES_DISTANCIA)));
        int indiceVelocidade = (int)Math.abs(velocidade / 2);
        int indiceUltimaVelocidade = (int)Math.abs(ultimaVelocidade / 2);
        buffer = buffersEstatisticas[indiceDistancia][indiceVelocidade][indiceUltimaVelocidade];
    }

    private void avancar() {
        distanciaPercorrida += UtilitariosGFT.velocidadeDoProjetil(potenciaProjetil);
    }

    private boolean jaChegou() {
        return distanciaPercorrida > localArma.distance(localAlvo) - 18;
    }

    private int binAtual() {
        int bin = (int)Math.round(((Utils.normalRelativeAngle(UtilitariosGFT.anguloAbsoluto(localArma, localAlvo) - angulo)) /
                (direcaoLateral * LARGURA_BIN)) + BIN_MEIO);
        return UtilitariosGFT.minMax(bin, 0, BINS - 1);
    }

    private int binMaisVisitado() {
        int maisVisitado = BIN_MEIO;
        for (int i = 0; i < BINS; i++) {
            if (buffer[i] > buffer[maisVisitado]) {
                maisVisitado = i;
            }
        }
        return maisVisitado;
    }
}
