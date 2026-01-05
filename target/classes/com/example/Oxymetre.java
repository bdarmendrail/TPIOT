public class Oxymetre extends Capteur {
    
    private double saturationOxygene;

    public Oxymetre( double saturationOxygene) {
        this.setValue(saturationOxygene);
        //this.saturationOxygene = saturationOxygene;
    }

    public double getSaturationOxygene() {
        return saturationOxygene;
    }

    public void setSaturationOxygene(double saturationOxygene) {
        this.setValue(saturationOxygene);
        //this.saturationOxygene = saturationOxygene;
    }
}
