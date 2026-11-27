package br.unijorge.urnacidada;

import br.unijorge.urnacidada.controller.UrnaController;
import br.unijorge.urnacidada.model.Cargos;
import br.unijorge.urnacidada.service.CandidatoService;
import br.unijorge.urnacidada.service.RepositorioVotacao;
import br.unijorge.urnacidada.service.RepositorioLocal;
import java.nio.file.Path;
import br.unijorge.urnacidada.service.VotacaoService;
import br.unijorge.urnacidada.service.firestore.ClienteFirestore;
import br.unijorge.urnacidada.service.firestore.ConfiguracaoException;
import br.unijorge.urnacidada.service.firestore.ConfiguracaoFirebase;
import br.unijorge.urnacidada.service.firestore.RepositorioFirestore;
import br.unijorge.urnacidada.view.JanelaUrna;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Ponto de entrada: monta as camadas (Firestore, serviços, controller e view) e abre a urna. */
public final class Main {

    private Main() { }

    public static void main(String[] args) {
        boolean firebase = java.util.Arrays.asList(args).contains("--firebase");
        SwingUtilities.invokeLater(() -> iniciar(firebase));
    }

    private static void iniciar(boolean firebase) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignorada) {
            // segue com o visual padrão
        }
        RepositorioVotacao repositorio;
        if (firebase) {
            try {
                repositorio = new RepositorioFirestore(new ClienteFirestore(ConfiguracaoFirebase.deAmbiente()));
            } catch (ConfiguracaoException e) {
                JOptionPane.showMessageDialog(null, e.getMessage(), "Firebase não configurado", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } else {
            repositorio = new RepositorioLocal(Path.of("dados", "votos"));
        }
        Executor segundoPlano = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "urna-persistencia");
            t.setDaemon(true);
            return t;
        });
        UrnaController controller = new UrnaController(new CandidatoService(repositorio),
                new VotacaoService(repositorio), Cargos.sequencia(), segundoPlano, SwingUtilities::invokeLater);

        new JanelaUrna(controller).setVisible(true);
        controller.inicializar();
    }
}
