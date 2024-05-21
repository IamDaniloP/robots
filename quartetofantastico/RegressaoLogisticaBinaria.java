package quartetofantastico;

public class RegressaoLogisticaBinaria {
    private static double funcaoLinear(double distancia, double velocidade) {
        double beta1 = 0.05;    // Coeficiente para distancia
        double beta2 = -0.01;    // Coeficiente para velocidade
        double beta0 = -beta1 * 1200;  // Ajuste de intercepto para centrar a transição em distancia = 1200

        return (beta0 + beta1 * distancia + beta2 * velocidade);
    }

    private static double funcaoLogistica(double z) {
        return 1 / (1 + Math.exp(-z));
    }

    public static double getCoeficiente(double distancia, double velocidade) {
        double z = funcaoLinear(distancia, Math.abs(velocidade));
        return funcaoLogistica(z);
    }
}
