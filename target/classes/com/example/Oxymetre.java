public class Oxymetre {
    
    private String nom;
    private int age;
    private double saturationOxygene;

    public Oxymetre(String nom, int age, double saturationOxygene) {
        this.nom = nom;
        this.age = age;
        this.saturationOxygene = saturationOxygene;
    }

    public String getNom() {
        return nom;
    }

    public int getAge() {
        return age;
    }

    public double getSaturationOxygene() {
        return saturationOxygene;
    }

    public void setSaturationOxygene(double saturationOxygene) {
        this.saturationOxygene = saturationOxygene;
    }
}
