package br.com.gsordi.techchallenge3.agendamento.usuario;

public enum Perfil {
    MEDICO,
    ENFERMEIRO,
    PACIENTE;

    public String getRole() {
        return "ROLE_" + this.name();
    }
}