public class Capteur{
    private Patient patient;
    private double value;

    public Capteur(Patient pt){
        this.patient = pt;
    }
    public void setValue(double val){
        this.value = val;
    }
}