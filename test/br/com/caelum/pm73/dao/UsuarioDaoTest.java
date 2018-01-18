package br.com.caelum.pm73.dao;

import static org.junit.Assert.*;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import br.com.caelum.pm73.dominio.Usuario;

public class UsuarioDaoTest {

	private Session session;
	private UsuarioDao usuarioDao;

	@Before // antes de todos os testes
	public void setup() {
		session = new CriadorDeSessao().getSession();
		usuarioDao = new UsuarioDao(session);

		// List<Usuario> todos = usuarioDao.todos();
		// Transaction transaction = session.beginTransaction();
		// for (Usuario usu : todos) {
		// usuarioDao.deletar(usu);
		// }
		// transaction.commit();

		session.beginTransaction();
	}

	@After // depois de todos os testes
	public void close() {
		session.getTransaction().rollback();
		session.close();
	}

	@Test
	public void deveEncontrarPeloNomeEEmail() {
		Usuario usuarioTeste = new Usuario("Joao da Silva", "joao@dasilva.com.br");
		usuarioDao.salvar(usuarioTeste);

		Usuario usuario = usuarioDao.porNomeEEmail("Joao da Silva", "joao@dasilva.com.br");

		assertEquals("Joao da Silva", usuario.getNome());
		assertEquals("joao@dasilva.com.br", usuario.getEmail());

		usuarioDao.deletar(usuario);
	}

	@Test
	public void deveRetornarNuloSeNaoEncontrarUsuario() {
		Usuario usuario = usuarioDao.porNomeEEmail("Joao da Silva", "joao@dasilva.com.br");

		assertNull(usuario);
	}

	// @Test
	public void deveEncontrarPeloNomeEEmailMockado() {
		Query query = Mockito.mock(Query.class);

		UsuarioDao usuarioDao = new UsuarioDao(session);

		Usuario usuario = new Usuario("Joao da Silva", "joao@dasilva.com.br");

		String sql = "from Usuario u where u.nome = :nome and u.email = :email";
		Mockito.when(session.createQuery(sql)).thenReturn(query);
		Mockito.when(query.uniqueResult()).thenReturn(usuario);
		Mockito.when(query.setParameter("nome", "Joao da Silva")).thenReturn(query);
		Mockito.when(query.setParameter("email", "joao@dasilva.com.br")).thenReturn(query);

		Usuario usuarioDoBanco = usuarioDao.porNomeEEmail("Joao da Silva", "joao@dasilva.com.br");

		assertEquals(usuario.getNome(), usuarioDoBanco.getNome());
		assertEquals(usuario.getEmail(), usuarioDoBanco.getEmail());
	}

	@Test
	public void deveDeletarUmUsuario() {
		Usuario usuario = new Usuario("Joao da Silva", "joao@dasilva.com.br");

		usuarioDao.salvar(usuario);

		usuarioDao.deletar(usuario);

		// Possivel falha do hibernate
		session.flush();
		session.clear();

		assertEquals(0, usuarioDao.todos().size());

	}

	@Test
	public void deveRetornarOUsuarioAlterado() {

		Usuario usuario = new Usuario("Joao da Silva", "joao@dasilva.com.br");

		usuarioDao.salvar(usuario);
		
		session.flush();
		
		usuario.setNome("David");
		usuario.setEmail("x@x.com");
		
		usuarioDao.atualizar(usuario);

		session.flush();
		
		assertEquals("David", usuarioDao.porNomeEEmail("David", "x@x.com").getNome());
		assertNull(usuarioDao.porNomeEEmail("Joao da Silva", "joao@dasilva.com.br"));
	}

}
