package br.unijorge.urnacidada;

import br.unijorge.urnacidada.controller.UrnaController;
import br.unijorge.urnacidada.model.Cargos;
import br.unijorge.urnacidada.service.CandidatoService;
import br.unijorge.urnacidada.service.RepositorioVotacao;
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
        SwingUtilities.invokeLater(Main::iniciar);
    }

    private static void iniciar() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignorada) {
            // segue com o visual padrão
        }
        ConfiguracaoFirebase config;
        try {
            config = ConfiguracaoFirebase.deAmbiente();
        } catch (ConfiguracaoException e) {
            JOptionPane.showMessageDialog(null,
                    e.getMessage() + "\n\nDefina FIREBASE_PROJECT_ID e FIREBASE_API_KEY antes de abrir a urna.\n"
                            + "Veja o README.md, seção \"Configuração do Firebase\".",
                    "Firebase não configurado", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
            return;
        }

        RepositorioVotacao repositorio = new RepositorioFirestore(new ClienteFirestore(config));
        Executor segundoPlano = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "urna-firestore");
            t.setDaemon(true);
            return t;
        });
        UrnaController controller = new UrnaController(new CandidatoService(repositorio),
                new VotacaoService(repositorio), Cargos.sequencia(), segundoPlano, SwingUtilities::invokeLater);

        new JanelaUrna(controller).setVisible(true);
        controller.inicializar();
    }
}
