package quartetofantastico;

import java.awt.geom.Point2D;

public class UtilitariosGFT {
    static double velocidadeDoProjetil(double potencia) {
        return 20 - 3 * potencia;
    }

    static Point2D projetarMov(Point2D localOrigem, double angulo, double comprimento) {
        return new Point2D.Double(localOrigem.getX() + Math.sin(angulo) * comprimento,
                localOrigem.getY() + Math.cos(angulo) * comprimento);
    }

    static double anguloAbsoluto(Point2D origem, Point2D destino) {
        return Math.atan2(destino.getX() - origem.getX(), destino.getY() - origem.getY());
    }

    static int sinal(double valor) {
        return valor < 0 ? -1 : 1;
    }

    static int minMax(int valor, int min, int max) {
        return Math.max(min, Math.min(max, valor));
    }
}
