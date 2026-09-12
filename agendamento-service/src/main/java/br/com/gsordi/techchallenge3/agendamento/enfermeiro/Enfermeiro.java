package br.com.gsordi.techchallenge3.agendamento.enfermeiro;

import br.com.gsordi.techchallenge3.agendamento.usuario.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "enfermeiros")
public class Enfermeiro {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String coren;

    @OneToOne
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    public Enfermeiro(String nome, String coren) {
        this.nome = nome;
        this.coren = coren;
    }

    public void associarUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}
