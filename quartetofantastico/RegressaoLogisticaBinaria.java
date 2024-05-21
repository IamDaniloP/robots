package quartetofantastico;

/*
distacia, velocidade e variável dependente (Y)
                {1300, 0, 1},
                {1500, 0, 1},
                {1100, 5, 0},
                {900, -6, 0},
                {800, 8, 0},
                {1300, 0, 1},
                {850, 7, 0},
                {1100, 1.7, 0},
                {1200, 0, 1},
                {1000, 0, 0},
                {1400, 0, 1},
                {1190, 4, 0},
                {1000, -2, 0},
                {1199, 0, 0},
                {1201, 6, 1},
                {1250, 0, 1},
                {1050, 7.5, 0},
                {1150, -5, 0},
                {1350, 0, 1},   
                {1205, 0, 1}

                Acima está o conjunto de dados utilizado para realizar o treinamento. Para isso, foi utilizado a ferramenta weka.
                Para que evitasse que o robô não funcionasse, decidi utilizar aqui apenas os coeficientes retirados do treinamento utilizando tal ferramenta.
                O weka realizou cerca uma quantidade de interações decidida pela ferramenta para treinar o conjunto. Assim, foi possível perceber
*/

public class RegressaoLogisticaBinaria {
    private static double funcaoLinear(double distancia, double velocidade) {
        double beta1 = 0.05;    // Coeficiente para distancia
        double beta2 = -0.01;    // Coeficiente para velocidade
        double beta0 = -beta1 * 1200;  // Foi necessário pois estava classificando muito abaixo do esperado, devido ao conjunto de dados pequeno para que não afetasse o tempo de execução

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
