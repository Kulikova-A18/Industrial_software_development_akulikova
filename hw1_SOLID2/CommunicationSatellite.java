public class CommunicationSatellite extends Satellite {
    public CommunicationSatellite(String name) {
        super(name, 80);
    }

    @Override
    public String getType() {
        return "Коммуникационный спутник";
    }

    @Override
    public void performSpecificMission() {
        System.out.println("Передача данных...");
        energy.consume(15);
        System.out.println("  Данные успешно переданы. Остаток энергии: " + energy.getBatteryLevel());
    }

    public void establishConnection() {
        if (state.isActive() && energy.hasEnoughEnergy(10)) {
            energy.consume(10);
            System.out.println("Спутник " + name + ": соединение установлено");
        }
    }
}