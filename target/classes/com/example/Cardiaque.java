public class Cardiaque extends Capteur {
    
    private double frequenceCardiaque;

    public Cardiaque( double frequenceCardiaque) {
       this.setValue(frequenceCardiaque)
       //this.frequenceCardiaque = frequenceCardiaque;
    }

    public double getFrequenceCardiaque() {
        return this.value;
    }

    public void setFrequenceCardiaque(double frequenceCardiaque) {
        this.setValue(frequenceCardiaque);
    }
}
