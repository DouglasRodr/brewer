CREATE TABLE cidade (
    codigo BIGINT(20) PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(50) NOT NULL,
    estado VARCHAR(2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO cidade (nome, estado) VALUES ('Rio Branco', 'AC');
INSERT INTO cidade (nome, estado) VALUES ('Cruzeiro do Sul', 'AC');
INSERT INTO cidade (nome, estado) VALUES ('Salvador', 'BA');
INSERT INTO cidade (nome, estado) VALUES ('Porto Seguro', 'BA');
INSERT INTO cidade (nome, estado) VALUES ('Santana', 'BA');
INSERT INTO cidade (nome, estado) VALUES ('Goiânia', 'GO');
INSERT INTO cidade (nome, estado) VALUES ('Itumbiara', 'GO');
INSERT INTO cidade (nome, estado) VALUES ('Novo Brasil', 'GO');
INSERT INTO cidade (nome, estado) VALUES ('Belo Horizonte', 'MG');
INSERT INTO cidade (nome, estado) VALUES ('Uberlândia', 'MG');
INSERT INTO cidade (nome, estado) VALUES ('Montes Claros', 'MG');
INSERT INTO cidade (nome, estado) VALUES ('Florianópolis', 'SC');
INSERT INTO cidade (nome, estado) VALUES ('Criciúma', 'SC');
INSERT INTO cidade (nome, estado) VALUES ('Camboriú', 'SC');
INSERT INTO cidade (nome, estado) VALUES ('Lages', 'SC');
INSERT INTO cidade (nome, estado) VALUES ('São Paulo', 'SP');
INSERT INTO cidade (nome, estado) VALUES ('Ribeirão Preto', 'SP');
INSERT INTO cidade (nome, estado) VALUES ('Campinas', 'SP');
INSERT INTO cidade (nome, estado) VALUES ('Santos', 'SP');