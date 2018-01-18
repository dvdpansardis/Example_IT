package br.com.caelum.pm73.dao;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.hibernate.Session;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import br.com.caelum.pm73.dominio.Lance;
import br.com.caelum.pm73.dominio.Leilao;
import br.com.caelum.pm73.dominio.LeilaoBuilder;
import br.com.caelum.pm73.dominio.Usuario;

public class LeilaoDaoTest {

	private Session session;
	private UsuarioDao usuarioDao;
	private LeilaoDao leilaoDao;

	@Before
	public void setup() {
		session = new CriadorDeSessao().getSession();
		usuarioDao = new UsuarioDao(session);
		leilaoDao = new LeilaoDao(session);
		
		session.beginTransaction();
	}

	@After
	public void close() {
		session.getTransaction().rollback();
		session.close();
	}
	
	
	@Test
	public void deveTrazerLeiloesNaoEncerradosNoPeriodo(){
		Calendar inicio = Calendar.getInstance();
		inicio.add(Calendar.DAY_OF_MONTH, -10);
		Calendar fim = Calendar.getInstance();
		
		Usuario david = new Usuario("David", "x@x.com");
		
		Leilao leilao1 = new LeilaoBuilder()
				.comDono(david)
				.comNome("Geladeira")
				.comValor(1500.0)
				.diasAtras(2)
				.constroi();

		Leilao leilao2 = new LeilaoBuilder()
				.comDono(david)
				.comNome("XBox")
				.comValor(700.0)
				.diasAtras(20)
				.constroi();
		
		usuarioDao.salvar(david);
		leilaoDao.salvar(leilao1);
		leilaoDao.salvar(leilao2);
		
		List<Leilao> porPeriodo = leilaoDao.porPeriodo(inicio, fim);
		
		assertEquals(1, porPeriodo.size());
		assertEquals(leilao1.getNome(), porPeriodo.get(0).getNome());
	}

	@Test
	public void naoDeveTrazerLeiloesEncerradosNoPeriodo(){
		Usuario david = new Usuario("David", "x@x.com");

		//Intevalo
		Calendar inicio = Calendar.getInstance();
		inicio.add(Calendar.DAY_OF_MONTH, -10);
		Calendar fim = Calendar.getInstance();
		
		//Encerrado dentro do intervalo
		Leilao leilao1 = new Leilao("Geladeira", 1500.0, david, false);
		Calendar dataDoLeilao1 = Calendar.getInstance();
		dataDoLeilao1.add(Calendar.DAY_OF_MONTH, -2);
		leilao1.setDataAbertura(dataDoLeilao1);
		leilao1.encerra();
		
		//Aberto dentro do intervalo
		Leilao leilao2 = new Leilao("XBox", 700.0, david, false);
		Calendar dataDoLeilao2 = Calendar.getInstance();
		dataDoLeilao2.add(Calendar.DAY_OF_MONTH, -5);
		leilao2.setDataAbertura(dataDoLeilao2);

		//Encerrado fora do intervalo
		Leilao leilao3 = new Leilao("PsOne", 700.0, david, false);
		Calendar dataDoLeilao3 = Calendar.getInstance();
		dataDoLeilao3.add(Calendar.DAY_OF_MONTH, -15);
		leilao3.setDataAbertura(dataDoLeilao3);
		
		//Aberto fora do intervalo
		Leilao leilao4 = new Leilao("PC", 700.0, david, false);
		Calendar dataDoLeilao4 = Calendar.getInstance();
		dataDoLeilao4.add(Calendar.DAY_OF_MONTH, -20);
		leilao4.setDataAbertura(dataDoLeilao4);
		
		usuarioDao.salvar(david);
		leilaoDao.salvar(leilao1);
		leilaoDao.salvar(leilao2);
		leilaoDao.salvar(leilao3);
		leilaoDao.salvar(leilao4);
		
		List<Leilao> porPeriodo = leilaoDao.porPeriodo(inicio, fim);
		
		assertEquals(1, porPeriodo.size());
		assertEquals("XBox", porPeriodo.get(0).getNome());
	}
	
	@Test
	public void deveTrazerLeiloesAbertosNoPeriodoComMaisDe3Lances(){
		Usuario david = new Usuario("David", "x@x.com");
		
		//Leilao aberto com mais de 3 lances
		Leilao leilao1 = new Leilao("pc", 2500.0, david, false);
		leilao1.adicionaLance(new Lance(Calendar.getInstance(), david, 10.0, leilao1));
		leilao1.adicionaLance(new Lance(Calendar.getInstance(), david, 100.0, leilao1));
		leilao1.adicionaLance(new Lance(Calendar.getInstance(), david, 1000.0, leilao1));
		leilao1.adicionaLance(new Lance(Calendar.getInstance(), david, 10000.0, leilao1));

		//leilao encerrado com mais de 3 lances
		Leilao leilao2 = new Leilao("xbox", 1500.0, david, false);
		leilao2.encerra();
		leilao2.adicionaLance(new Lance(Calendar.getInstance(), david, 10.0, leilao2));
		leilao2.adicionaLance(new Lance(Calendar.getInstance(), david, 100.0, leilao2));
		leilao2.adicionaLance(new Lance(Calendar.getInstance(), david, 1000.0, leilao2));
		leilao2.adicionaLance(new Lance(Calendar.getInstance(), david, 10000.0, leilao2));

		//leilao aberto com menos de 3 lances
		Leilao leilao3 = new Leilao("note", 1000.0, david, false);
		leilao3.adicionaLance(new Lance(Calendar.getInstance(), david, 10.0, leilao1));

		//leilao encerrado com menos de 3 lances
		Leilao leilao4 = new Leilao("tv", 1700.0, david, false);
		leilao4.encerra();
		leilao4.adicionaLance(new Lance(Calendar.getInstance(), david, 10.0, leilao4));
		
		usuarioDao.salvar(david);
		leilaoDao.salvar(leilao1);
		leilaoDao.salvar(leilao2);
		leilaoDao.salvar(leilao3);
		leilaoDao.salvar(leilao4);
		
		List<Leilao> porPeriodo = leilaoDao.disputadosEntre(500, 3000);
		
		assertEquals(1, porPeriodo.size());
		assertEquals("pc", porPeriodo.get(0).getNome());
	}
	
	@Test
	public void deveTrazerOLeilaoDoUsuario(){
		Usuario david = new Usuario("David", "dvd@x.com");
		Usuario leticia = new Usuario("leticia", "dvd@x.com");
		
		Leilao leilao1 = new LeilaoBuilder()
				.comDono(david)
				.comNome("Geladeira")
				.comValor(1500.0)
				.diasAtras(2)
				.constroi();
		leilao1.adicionaLance(new Lance(Calendar.getInstance(), david, 10.0, leilao1));
		
		Leilao leilao2 = new LeilaoBuilder()
				.comDono(leticia)
				.comNome("XBox")
				.comValor(700.0)
				.diasAtras(20)
				.constroi();
		leilao2.adicionaLance(new Lance(Calendar.getInstance(), leticia, 10.0, leilao2));
				
		usuarioDao.salvar(david);
		usuarioDao.salvar(leticia);
		leilaoDao.salvar(leilao1);
		leilaoDao.salvar(leilao2);
		
		List<Leilao> listaLeiloesDoUsuario = leilaoDao.listaLeiloesDoUsuario(david);
		
		assertEquals(1, listaLeiloesDoUsuario.size());
	}
	
	@Test
    public void listaDeLeiloesDeUmUsuarioNaoTemRepeticao() throws Exception {
        Usuario dono = new Usuario("Mauricio", "m@a.com");
        Usuario comprador = new Usuario("Victor", "v@v.com");
        Leilao leilao = new LeilaoBuilder()
            .comDono(dono)
            .constroi();
        
        leilao.adicionaLance(new Lance(Calendar.getInstance(), comprador, 100.0, leilao));
        leilao.adicionaLance(new Lance(Calendar.getInstance(), comprador, 200.0, leilao));
        
        usuarioDao.salvar(dono);
        usuarioDao.salvar(comprador);
        leilaoDao.salvar(leilao);

        List<Leilao> leiloes = leilaoDao.listaLeiloesDoUsuario(comprador);
        assertEquals(1, leiloes.size());
        assertEquals(leilao, leiloes.get(0));
    }
	
	@Test
	public void deveContarLeiloesNaoEncerrados(){
		Usuario david = new Usuario("David", "dvd@x.com");
		
		usuarioDao.salvar(david);
		
		Leilao ativo = new Leilao("Geladeira", 1500.0, david, false);
		Leilao encerrado = new Leilao("XBox", 700.0, david, false);
		encerrado.encerra();
		
		leilaoDao.salvar(ativo);
		leilaoDao.salvar(encerrado);
		
		long total = leilaoDao.total();
		
		assertEquals(1L, total);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void deveRetornar1400DeMedia(){
		Usuario david = new Usuario("David", "dvd@x.com");
		
		usuarioDao.salvar(david);
		
		Leilao leilao1 = new Leilao("Geladeira", 2000.0, david, false);
		leilao1.adicionaLance(new Lance(Calendar.getInstance(), david, 100.0, leilao1));
		leilao1.adicionaLance(new Lance(Calendar.getInstance(), david, 1000.0, leilao1));
		
		Leilao leilao2 = new Leilao("XBox", 700.0, david, false);
		leilao2.adicionaLance(new Lance(Calendar.getInstance(), david, 10.0, leilao2));
		
		Leilao leilao3 = new Leilao("PC", 1500.0, david, false);
		leilao3.adicionaLance(new Lance(Calendar.getInstance(), david, 10000.0, leilao3));
		
		leilaoDao.salvar(leilao1);
		leilaoDao.salvar(leilao2);
		leilaoDao.salvar(leilao3);
		
		double valorInicialMedioDoUsuario = leilaoDao.getValorInicialMedioDoUsuario(david);
		
		assertEquals(1400, valorInicialMedioDoUsuario, 0.0001);
	}

	@Test
	public void deveContarDoisLeiloesoEncerrados(){
		Usuario david = new Usuario("David", "dvd@x.com");
		
		usuarioDao.salvar(david);
		
		Leilao encerrado1 = new Leilao("Geladeira", 1500.0, david, false);
		Leilao encerrado2 = new Leilao("XBox", 700.0, david, false);
		encerrado1.encerra();
		encerrado2.encerra();
		
		leilaoDao.salvar(encerrado1);
		leilaoDao.salvar(encerrado2);
		
		long total = leilaoDao.total();
		
		assertEquals(0L, total);
	}
	
	@Test 
	public void deveRetornarUmLeilaoUsado(){
		Usuario david = new Usuario("David", "dvd@x.com");
		
		usuarioDao.salvar(david);
		
		Leilao prodNovo = new Leilao("Geladeira", 1500.0, david, false);
		Leilao prodUsado = new Leilao("XBox", 700.0, david, true);
		
		leilaoDao.salvar(prodNovo);
		leilaoDao.salvar(prodUsado);
		
		List<Leilao> novos = leilaoDao.novos();
		
		assertEquals(1, novos.size());
	}
	
	@Test
	public void deveRetornarLeiloesAntigos(){
		Usuario david = new Usuario("David", "dvd@x.com");
		
		usuarioDao.salvar(david);
		
		Leilao leilaoAntigo = new Leilao("Geladeira", 1500.0, david, false);
		Leilao leilaoNovo = new Leilao("XBox", 700.0, david, true);
		Leilao leilaoNoLimite = new Leilao("XBox", 700.0, david, true);

		Calendar antigo = Calendar.getInstance();
		antigo.add(Calendar.DAY_OF_MONTH, -10);
		leilaoAntigo.setDataAbertura(antigo);

		Calendar noLimite = Calendar.getInstance();
		antigo.add(Calendar.DAY_OF_MONTH, -7);
		leilaoNoLimite.setDataAbertura(noLimite);

		Calendar novo = Calendar.getInstance();
		leilaoNovo.setDataAbertura(novo);
		
		
		leilaoDao.salvar(leilaoAntigo);
		leilaoDao.salvar(leilaoNovo);
		leilaoDao.salvar(leilaoNoLimite);
		
		List<Leilao> antigos = leilaoDao.antigos();
		
		assertEquals(1L, antigos.size());
	}
	
	@Test
	public void deveDeletarUmLeilao(){
		Usuario david = new Usuario("David", "dvd@x.com");
		
		usuarioDao.salvar(david);
		
		Leilao leilao = new Leilao("Geladeira", 1500.0, david, false);
		
		leilaoDao.salvar(leilao);
		leilaoDao.deleta(leilao);
		
		session.flush();
		session.clear();
	
		Leilao leilaoDoBanco = leilaoDao.porId(leilao.getId());
		
		assertNull(leilaoDoBanco);
	}
	
	
}
