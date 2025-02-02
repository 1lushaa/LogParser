package backend.academy.analyzer.render;

import lombok.AccessLevel;
import lombok.Getter;

class AdocRendererTest extends AbstractRendererTest {

    @Getter(AccessLevel.PROTECTED)
    private final AbstractRenderer renderer = new AdocRenderer();

    @Getter(AccessLevel.PROTECTED)
    private final String expectedOutput = """
        === General information

        | Metric                | Value                                                                                                     |
        |:---------------------:|:---------------------------------------------------------------------------------------------------------:|
        | File(-s)              | `https://raw.githubusercontent.com/elastic/examples/master/Common%20Data%20Formats/nginx_logs/nginx_logs` |
        | Starting date         | 2015-05-17T08:05:35Z                                                                                      |
        | Ending date           | 2015-05-17T08:06:55Z                                                                                      |
        | Number of requests    | 42                                                                                                        |
        | AverageResponseSize   | 201                                                                                                       |
        | 95p response's size's | 0                                                                                                         |

        === Requested resources

        | Resource               | Requests |
        |:----------------------:|:--------:|
        | `/downloads/product_1` | 24       |
        | `/downloads/product_2` | 18       |

        === Responses codes

        | Code  | Count |
        |:-----:|:-----:|
        | `304` | 26    |
        | `404` | 14    |
        | `200` | 2     |

        === Remote addresses

        | Address           | Count |
        |:-----------------:|:-----:|
        | `80.91.33.133`    | 10    |
        | `173.203.139.108` | 5     |
        | `50.57.209.92`    | 5     |

        === Http referers

        | Referer | Count |
        |:-------:|:-----:|
        | `-`     | 42    |

        """.replace("\n", System.lineSeparator());

}
