package com.emaxsoftware.popreel.ui.preview

import com.emaxsoftware.popreel.core.model.Movie
import com.emaxsoftware.popreel.core.model.MovieTag
import com.emaxsoftware.popreel.core.model.TagStyle

/**
 * Static content mirroring the Stitch designs, used for Compose previews and the
 * profile avatar. Live screens load from the catalog API (see MovieRepository).
 */
object SampleCatalog {

    const val PROFILE_AVATAR_URL =
        "https://lh3.googleusercontent.com/aida-public/AB6AXuDgvAfsjX-CGX7-nEzt_TYokgZtXuWndyGkwKc6IngGLG9QDABeVvOVL2TMiF0IkH3sHrOX9DS2c3UhycOv9Hd9bZ3DmPmJquBqjPUDuiI93cpQDSJwk2nwXNXiC7INtEVzVutliRiiGU4yfEL7dksY5Ft531D8Kv0Isb5-BplqHCnzhPJEtt64tN-kV3mvAF1vXFUb-Z_tRNLPiq2KjiU8Cb0beJmeM-L19Te_HQ4j"

    val genres = listOf(
        "All",
        "Sci-Fi & Cyberpunk",
        "Psychological Thriller",
        "Neo-Noir",
        "Animation",
        "Indie Drama",
        "Action Adventure",
    )

    val featured = Movie(
        id = "dune-part-two",
        title = "Dune: Part Two",
        director = "Denis Villeneuve",
        year = 2024,
        rating = 8.8,
        genres = "Sci-Fi • Epic",
        runtime = "2h 46m",
        posterUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCfDztre1szvqn13m8HVBC3wMmh1oUSZDhCbe66Iyi5-xy4ltH9fP5VCJMXoCOW9KVVRszEqOSBdajFKsny9yEKYMjW7NzzMbtpydulnsIBvYlQMTNsHCJZymjNp3mswNXwtEnGDjlpnz6L2N9KJ9KjiNfsb9Tr2HcMR9pKZFU2tN8l1vdo4Gr9fdiZBAq9S8OqrJDb_9ijj0J6aUzwhgXj8NXBWY-XjG_RRxZSIpt7",
        badge = MovieTag("99% Match", TagStyle.Positive),
        synopsis = "Paul Atreides unites with Chani and the Fremen while seeking revenge against " +
            "the conspirators who destroyed his family. Facing a choice between the love of his " +
            "life and the fate of the known universe, he endeavors to prevent a terrible future " +
            "only he can foresee.",
    )

    val spotlight = listOf(
        Movie(
            id = "oppenheimer", title = "Oppenheimer", director = "Christopher Nolan",
            directorShort = "Nolan", year = 2023, rating = 8.9, genres = "Historical • Drama",
            posterUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCX22GGiTkvh99GhfKSIAL3rdXoDmOvG5Eq4ZthaFKivJc0IylU8MUqxjjDJXC10ITw4B3FYMbs1EeqH6xF6djL5npbown5djpY2Vm7WruMxHYuHkW_4eE73CFMNBrJUJqeZHjwvnugHrfXyVXg2soA7QiAYYPWOxyc9Vl4OD3QyaHX7yaPGGHC8FtpkENzn0PXj9JKiG0gAievphtTRmh2iw9qaa9H37sBboRwBYMi",
            badge = MovieTag("RT 93%", TagStyle.Accent),
        ),
        Movie(
            id = "poor-things", title = "Poor Things", director = "Yorgos Lanthimos",
            directorShort = "Lanthimos", year = 2023, rating = 8.0, genres = "Surreal • Comedy",
            posterUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBQm07kAMyVOy7qWo9jjPBUDr6zLKkbJKYBDPXt-B2-o58_0J4Pg1S5iLZhm4l_QBWDz5wL824KxNDFY_61oQ__1NycvZ5C7l9Er7guRLEbrXl-6JSuJcQ9_i8QJT8fhdo56nTnRqxdiFtC8Hw56LdmjM3BiJJPMxcnxtRKo2T2KwP0qN-xplnRXxh_5RoIPx-RcUQLpuM9_0YMUQlQ6GUAHphXxdL8jbT8caiaKYtz",
            badge = MovieTag("96% Match", TagStyle.Positive),
        ),
        Movie(
            id = "civil-war", title = "Civil War", director = "Alex Garland",
            directorShort = "Garland", year = 2024, rating = 7.4, genres = "Action • Dystopia",
            posterUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBtXQTDoSwn6jbDfrfET-SxArAu1FeoagFocPbEqGysNbqoNzvtgpiCqK1rWEauZf52LMZIAs2DakPoWsQAbkrBjaKAzeBqSLUXRuUkAJqPXO1Pm0Z-7EDeTTQyVNXIc9v5J9ut-XQwrtdcYRQL8KNYs9eR_6_pnJpn6YDiPpYZZT3L1OtXyUI_k86joWp0V_4d6b1brB47mZOM7EHhbJ5GgqfMSzJUbsGqerTmt-Z-",
            badge = MovieTag("RT 81%", TagStyle.Accent),
        ),
        Movie(
            id = "challengers", title = "Challengers", director = "Luca Guadagnino",
            directorShort = "Guadagnino", year = 2024, rating = 7.7, genres = "Romance • Sport",
            posterUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBgtAF1uVClBYgsJGrVa1x9aX6Ik6clE7krH43yO3aaerXhP2w1U_EHr47bnKlh5DV0uNWsUiYrjndZWYNmQy9DU7ifNgsX4iQMYP9fbjgRyxE87BvRL72PCOk-s7aA8yfqYTPEiFedJZxKALDgn1xDh5nyjBaQ0t4ocwjjO9M5MbnXx5bseg5uVXD-zrY3FpcTiRi_HbztATNU4aRo15ppmC6s7rg_9w8AAf38_s7q",
            badge = MovieTag("91% Match", TagStyle.Positive),
        ),
    )

    val criticallyAcclaimed = listOf(
        Movie(
            id = "blade-runner-2049", title = "Blade Runner 2049", director = "Denis Villeneuve",
            year = 2017, rating = 8.0, genres = "Neo-Noir • Sci-Fi",
            posterUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBP7jQ3Ha09wnGWJhiShrDKLFV5M9KOcpve5j8vB-Ub0XMy0jN3qtn2d3OL_AMzj1FwhwsKEth0H3wXnbvkMLMf-pr0nteMIeN5fSmaiOJqdGbhDxXvOSR5GkSGj8Rjmlt7KxE89SNcBN1l9hLWOaVsC39pb2QfZA-ZbfbZCO9Osx5Ltjqi2wGC2gkMQUxZjoIrlrfKtKajjnk5jrk_DAe3eBtF_bgCdu5h4qfNqQq2",
            badge = MovieTag("98% Match", TagStyle.Accent),
            caption = "Neo-Noir", captionTag = MovieTag("4K HDR", TagStyle.Neutral),
        ),
        Movie(
            id = "past-lives", title = "Past Lives", director = "Celine Song",
            year = 2023, rating = 7.9, genres = "Romance • Drama",
            posterUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuA7Fh_eTJRg4jJoSqXRKT3et1kre55D11wWKLK3eRiuHNN5H4X2AuD4Cj0dhib7YBymJomRRvP9OHdQi859Neo_ywxaL0KlF_nwcgKwF6qDwC02TDm4OUBY-MPKOqQoscGfYEDwjgFsXxtvvwcAlqdQDJd3ve1RV_uUrp8_vIQxKvRWPCRbyBNUp2Fwed1de-ON6hRYoNpkNsIIQ2RNTojtANcoMKYKTU_XuutNp0sD",
            badge = MovieTag("95% Match", TagStyle.Positive),
            caption = "A24 Classic", captionTag = MovieTag("Top Pick", TagStyle.Accent),
        ),
        Movie(
            id = "eeaao", title = "Everything Everywhere", director = "Daniels",
            year = 2022, rating = 7.8, genres = "Multiverse • Action",
            posterUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAVH6lXRQukScx22MzP8s2T7ZKuRSuackcadCQd8AD2_5mroRnx-il9FJUYnRGJqA8xq5URp-R44uXABQK-yl7N06tXAuyLIEvzY1bMoJGY3UUumR_757JYcZt4YhBLYTTfopY5JWnKW455lf77riLUYlJPX4WUoPwDfeiLv-9rp__pVXCLGUxZbYGYq-dBGmXAWlVtF4RSqwGlJU3GNN_ihp6mxP0-uGHzKb3YrOvA",
            badge = MovieTag("Oscar Winner", TagStyle.Accent),
            caption = "Multiverse", captionTag = MovieTag("97% Match", TagStyle.Positive),
        ),
        Movie(
            id = "parasite", title = "Parasite", director = "Bong Joon-ho",
            year = 2019, rating = 8.5, genres = "Thriller • Black Comedy",
            posterUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuD8jY99cmT7fHryVRQw-1QDNPEo7jczvyTMNZIi9EIQWcBg1RP-WAN81m-y2_P3GB72wLUpzd5fYmoaDne2Jgg6Q3TKuGc3JzMF3BCa6lXDjsDRPZCfJPke224sbundfA4lsg03qYrqLiOcfltJKIcqgvmnzSjPBMQ7EIn54Xke1E5eI4IiwEKSh6QXq7fmdnhdG7XsaJ9G-9tVMkWs2IdDzBlDLzPW1kzzKZfzPD7d",
            badge = MovieTag("Palme d'Or", TagStyle.Accent),
            caption = "Neo-Thriller", captionTag = MovieTag("Masterwork", TagStyle.Accent),
        ),
        Movie(
            id = "across-the-spider-verse", title = "Across the Spider-Verse",
            director = "Joaquim Dos Santos", directorShort = "Dos Santos",
            year = 2023, rating = 8.6, genres = "Animation • Action",
            posterUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBejGARrlfn-Hq7FKTCTY65Rpce0mbyYHmP2ZPFSrLs-ieeHuYteC_SE-vrhCBCKuM_exoaE2k8B5HBKaNcVPGIMR7faXffRAIWEHVE3y1FCnpIY_t8ARwaqcQGbqSPqVO6bxWSosMUgElhQqW9vXz-T6hi2lXkfVbIorka50PrRGZfor_1j8wvjMRmpdqS1zgQDHlch69OtjiaxQAzZs84tsr4wZrBDokluJ7EyeHp",
            badge = MovieTag("99% Match", TagStyle.Positive),
            caption = "Animated Art", captionTag = MovieTag("Spatial Audio", TagStyle.Neutral),
        ),
        Movie(
            id = "interstellar", title = "Interstellar", director = "Christopher Nolan",
            directorShort = "Nolan", year = 2014, rating = 8.7, genres = "Sci-Fi • Adventure",
            posterUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuB_l_-rAEsiZ3it3wV9tgpb1c2eFnY1sinzPnq0G4JSEjq8mW1ZBhhKCtXp3YH7b6w1a0Sv3wnzcJ7BGZPLIk0H_gFooqXEeN21FD32IEOdlscadC-nR-PUeAdMFTdh2Avg1obs4Uc8htaA87C5MysHOKQ2poM7v0wH4xN5wi42izA_uRmMKNv8rKk0tuOUgVzZCwdbfWj9YZDURnELDI0vt65B22tcMYu-XDkP397S",
            badge = MovieTag("Top 20", TagStyle.Accent),
            caption = "Cosmic Scope", captionTag = MovieTag("IMAX 70mm", TagStyle.Accent),
        ),
    )

    // region Swipe Discovery Deck

    /**
     * Swipe queue. Scores and runtimes are illustrative
     * sample values, not live data.
     */
    val swipeQueue: List<Movie> = listOf(
        Movie(
            id = "arrival", title = "Arrival", director = "Denis Villeneuve",
            directorShort = "Villeneuve", year = 2016, rating = 8.2,
            genres = "Sci-Fi • Mystery • Drama", runtime = "1h 56m",
            posterUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBA3i66WAY5KP4X0MXw3pNcyZZscWt8HI2pqDkuD4iJwGCc9gkMlGt8Bod8jsVJ4r5sfVj_laj1P2RZ2jf_W4m-BPMXuuwjXA2vezGFBvgU_eQj5a1CIvoaUo7m-3h544k8-O894jQNpY976yH-HaiVrcTr0qHlto9lUkiE82_IZ9IfUHMtpDudLT0KqJ8dse5-S-zO7dCsixG8_3t_NVYVG4_nMGLLWsdqvPfgn-Rh",
            synopsis = "A linguist works with the military to communicate with alien lifeforms " +
                "after twelve mysterious spacecraft appear around the world.",
        ),
        spotlight[1].copy(
            posterUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBhZyqmuZhr1BnfKt0FvgN6_6-Gv58PWxZ8VPDw6Xijl0d8z3j8WWfROBz1Cb1N_NWChOQ2R7_LAK5UPoMIOlT1QsZE-x2vYUHKjAxwQsD1F9hTXNewMxUhT4-CRbYxmtjWjZzx2urObdKqGTYo4VR2ra8a_i8jl1Rwcts_sd7uie4-TmCulpvanHp5CKuE13_lIWJLpw0mEZb8MNwZqIMT_nC2kdU8K9XQyWGECSCW",
            genres = "Sci-Fi • Comedy • Romance", runtime = "2h 21m",
            synopsis = "The fantastical evolution of Bella Baxter, a young woman brought back to " +
                "life by a brilliant and unorthodox scientist.",
        ),
        spotlight[3].copy(
            genres = "Drama • Romance • Sport", runtime = "2h 11m",
            synopsis = "Tashi, a former tennis prodigy turned coach, enters her husband into a " +
                "challenger event against her former lover.",
        ),
        spotlight[0].copy(
            runtime = "3h 0m",
            synopsis = "The story of J. Robert Oppenheimer, the race to build the atomic bomb, " +
                "and the reckoning that followed.",
        ),
        criticallyAcclaimed[1].copy(
            runtime = "1h 46m",
            synopsis = "Two deeply connected childhood friends are reunited in New York for one " +
                "fateful week, decades after they were separated.",
        ),
        criticallyAcclaimed[5].copy(
            runtime = "2h 49m",
            synopsis = "A team of explorers travels through a wormhole in space in an attempt " +
                "to ensure humanity's survival.",
        ),
    )

    // endregion
}
