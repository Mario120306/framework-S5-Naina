package mg.teste;

import itu.sprint.annotation.HandleURL;

public class Teste {
    @HandleURL("/hello")
    public void hello() {}

    @HandleURL("/teste")
    public void about() {}
}