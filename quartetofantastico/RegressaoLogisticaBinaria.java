package quartetofantastico;

/*
distância, velocidade e variável dependente (Y)
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

Acima está o conjunto de dados utilizado para realizar o treinamento.
Para isso, foi utilizada a ferramenta Weka.
Para evitar problemas com bibliotecas externas, decidi utilizar aqui apenas os coeficientes
obtidos do treinamento com essa ferramenta.
*/

public class RegressaoLogisticaBinaria {
    private static double funcaoLinear(double distancia, double velocidade) {
        double beta1 = 0.648;    // Coeficiente para distancia 12.96
        double beta2 = -0.1269;    // Coeficiente para velocidade
        double intercepto = -777.6; // Termo de interceptação
        return (intercepto + beta1 * distancia + beta2 * velocidade);
    }

    private static double funcaoLogistica(double z) {
        return 1 / (1 + Math.exp(-z));
    }

    public static double getCoeficiente(double distancia, double velocidade) {
        double z = funcaoLinear(distancia, Math.abs(velocidade));
        return funcaoLogistica(z);
    }
}
