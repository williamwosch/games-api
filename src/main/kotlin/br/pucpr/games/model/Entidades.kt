package br.pucpr.games.model

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
class Categoria(
    @Column(nullable = false, unique = true, length = 50)
    var nome: String
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}

@Entity
class Usuario(
    @Column(nullable = false)
    var nome: String,
    @Column(nullable = false, unique = true)
    var email: String,
    @Column(nullable = false)
    var senha: String?
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}

@Entity
class Jogo(
    @Column(nullable = false)
    var titulo: String,
    @Column(length = 1000)
    var descricao: String?,
    var anoLancamento: Int,
    @Column(nullable = false)
    var preco: BigDecimal
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    // many-to-many: tabela jogo_categoria é criada automaticamente
    @ManyToMany
    @JoinTable(
        name = "jogo_categoria",
        joinColumns = [JoinColumn(name = "jogo_id")],
        inverseJoinColumns = [JoinColumn(name = "categoria_id")]
    )
    var categorias: MutableSet<Categoria> = mutableSetOf()

    // one-to-many: ao apagar o jogo, apaga avaliações e favoritos dele
    @OneToMany(mappedBy = "jogo", cascade = [CascadeType.REMOVE])
    var avaliacoes: MutableList<Avaliacao> = mutableListOf()

    @OneToMany(mappedBy = "jogo", cascade = [CascadeType.REMOVE])
    var favoritos: MutableList<Favorito> = mutableListOf()
}

@Entity
class Avaliacao(
    var nota: Int,
    @Column(length = 500)
    var comentario: String?,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    var jogo: Jogo,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    var usuario: Usuario,
    var dataAvaliacao: LocalDateTime = LocalDateTime.now()
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}

// many-to-many "com tabela no meio": tem dado próprio (dataFavoritado)
@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["usuario_id", "jogo_id"])])
class Favorito(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    var usuario: Usuario,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    var jogo: Jogo,
    var dataFavoritado: LocalDateTime = LocalDateTime.now()
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}