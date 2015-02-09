package org.nohope.cassandra.mapservice

/**
 */
final class QuoteTestGenerator {
    private final static Random rand = new Random();
    private final static String PATTERN = "%s \' %s \' %s";

    private final static quotes = [
            """I had finished eating and watched her through the smoke of my cigarette.""",
            """It's what has happened to the people here on Earth in the last fifty years that really counts.
       When I was born, young man, we had just gone through the last World War.
       It was a low point in history - but it was the end of nationalism.
       Earth was too small for nations and they began grouping themselves into Regions.
       It took quite a while. When I was born the United States of America
       was still a nation and not merely a part of the Northern Region.
       In fact, the name of the corporation is still 'United States Robots-.'
       And the change from nations to Regions, which has stabilized our economy and
       brought about what amounts to a Golden Age,
       when this century is compared with the last, was also brought about by our robots.""",
            """You mean the Machines," I said. "The Brain you talked about was the first of the Machines, wasn't it?""",
            """Yes, it was, but it's not the Machines I was thinking of. Rather of a man. He died last year."
       Her voice was suddenly deeply sorrowful. "Or at least he arranged to die, because he knew we needed
       him no longer. Stephen Byerley.""",
            """Yes, I guessed that was who you meant.""",
            """He first entered public office in 2032. You were only a boy then, so you wouldn't remember
       the strangeness of it. His campaign for the Mayoralty was certainly the queerest in history-!""",
            """Francis Quinn was a politician of the new school. That, of course, is a meaningless expression,
       as are all expressions of the sort. Most of the "new schools" we have were duplicated in the social
       life of ancient Greece, and perhaps, if we knew more about it, in the social life of ancient Sumeria
       and in the lake dwellings of prehistoric Switzerland as well.""",
            """But, to get out from under what promises to be a dull and complicated beginning,
       it might be best to state hastily that Quinn neither ran for office nor canvassed for votes,
       made no speeches and stuffed no ballot boxes. Any more than Napoleon pulled a trigger at
       Austerlitz.""",
            """And since politics makes strange bedfellows, Alfred Lanning sat at the other side of the desk with
       his ferocious white eyebrows bent far forward over eyes in which chronic impatience had sharpened to
       acuity. He was not pleased.""",
            """The fact, if known to Quinn, would have annoyed him not the least.
       His voice was friendly, perhaps professionally so.""",
            """I assume you know Stephen Byerley, Dr. Lanning.""",
            """I have heard of him. So have many people.""",
            """Yes, so have I. Perhaps you intend voting for him at the next election."""
    ]

    private QuoteTestGenerator() {
    }

    public static String newQuote() {
        return String.format(PATTERN, UUID.randomUUID().toString(), quotes.get(rand.nextInt(quotes.size() - 1)), UUID.randomUUID().toString());
    }
}
