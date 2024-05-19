package quartetofantastico;

public class LogisticRegression {
    private static double linearFunction(double distancia, double velocidade) {
        double beta1 = 0.05;    // Coeficiente para distancia
        double beta2 = -0.01;    // Coeficiente para velocidade
        double beta0 = -beta1 * 1200;  // Ajuste de intercepto para centrar a transição em distancia = 1200

        return (beta0 + beta1 * distancia + beta2 * velocidade);
    }

    private static double logisticFunction(double z) {
        return 1 / (1 + Math.exp(-z));
    }

    public static double getCoefficient(double distancia, double velocidade) {
        double z = linearFunction(distancia, Math.abs(velocidade));
        return logisticFunction(z);
    }
}
