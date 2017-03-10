package moviestore.db.triplestore;

import java.util.List;
import java.util.function.Predicate;

import static com.google.common.collect.Lists.newArrayList;

public class Predicates {

    public static final List<String> MOVIE_FILTER =
            newArrayList(
                    "www.imdb.com"
            );


    public static final Predicate<String> filterByUrl = (url) ->
            MOVIE_FILTER.stream().anyMatch(pattern -> url.toLowerCase().contains(pattern));


}
