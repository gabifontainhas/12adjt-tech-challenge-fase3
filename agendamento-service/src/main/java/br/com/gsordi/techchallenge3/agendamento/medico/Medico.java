package br.com.gsordi.techchallenge3.agendamento.medico;


import br.com.gsordi.techchallenge3.agendamento.usuario.Usuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "medicos")
public class Medico {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String crm;

    @OneToOne
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    public Medico(String nome, String crm) {
        this.nome = nome;
        this.crm = crm;
    }

    public void associarUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}
