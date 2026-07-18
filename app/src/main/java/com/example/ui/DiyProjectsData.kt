package com.example.ui
import com.example.R

data class DiyProjectDetailsData(
    val id: String,
    val titleRes: Int,
    val category: String, // "shelter", "game", "cozy"
    val difficultyRes: Int,
    val costRes: Int,
    val timeRes: Int,
    val materialsRes: List<Int>,
    val descriptionRes: Int,
    val stepsRes: List<Int>,
    val altTextsRes: List<Int>
)

object DiyProjectsData {
    // Shared steps and alts for the newly added DIY projects
    private val sharedSteps = listOf(
        R.string.diy_step_gather,
        R.string.diy_step_measure,
        R.string.diy_step_cut_frame,
        R.string.diy_step_smooth_edges,
        R.string.diy_step_assemble_body,
        R.string.diy_step_add_comfort,
        R.string.diy_step_reinforce_joints,
        R.string.diy_step_add_decor,
        R.string.diy_step_find_spot,
        R.string.diy_step_cat_test
    )

    private val sharedAlts = listOf(
        R.string.diy_alt_gather,
        R.string.diy_alt_measure,
        R.string.diy_alt_cut_frame,
        R.string.diy_alt_smooth_edges,
        R.string.diy_alt_assemble_body,
        R.string.diy_alt_add_comfort,
        R.string.diy_alt_reinforce_joints,
        R.string.diy_alt_add_decor,
        R.string.diy_alt_find_spot,
        R.string.diy_alt_cat_test
    )

    val projects: List<DiyProjectDetailsData> = listOf(
        // ==================== SHELTER (10 Projects) ====================
        DiyProjectDetailsData(
            id = "shelter_1",
            titleRes = R.string.diy_s1_title,
            category = "shelter",
            difficultyRes = R.string.difficulty_easiest,
            costRes = R.string.cost_0,
            timeRes = R.string.time_5m,
            materialsRes = listOf(
                R.string.mat_newspaper, R.string.mat_stones, R.string.mat_sisal_twine, R.string.mat_flat_ground
            ),
            descriptionRes = R.string.diy_s1_desc,
            stepsRes = listOf(
                R.string.diy_s1_step1, R.string.diy_s1_step2, R.string.diy_s1_step3, R.string.diy_s1_step4,
                R.string.diy_s1_step5, R.string.diy_s1_step6, R.string.diy_s1_step7, R.string.diy_s1_step8,
                R.string.diy_s1_step9, R.string.diy_s1_step10
            ),
            altTextsRes = listOf(
                R.string.diy_s1_alt1, R.string.diy_s1_alt2, R.string.diy_s1_alt3, R.string.diy_s1_alt4,
                R.string.diy_s1_alt5, R.string.diy_s1_alt6, R.string.diy_s1_alt7, R.string.diy_s1_alt8,
                R.string.diy_s1_alt9, R.string.diy_s1_alt10
            )
        ),
        DiyProjectDetailsData(
            id = "shelter_2",
            titleRes = R.string.diy_s2_title,
            category = "shelter",
            difficultyRes = R.string.difficulty_easy,
            costRes = R.string.cost_2,
            timeRes = R.string.time_10m,
            materialsRes = listOf(
                R.string.mat_cardboard_box, R.string.mat_trash_bags, R.string.mat_duct_tape,
                R.string.mat_scissors, R.string.mat_straw
            ),
            descriptionRes = R.string.diy_s2_desc,
            stepsRes = listOf(
                R.string.diy_s2_step1, R.string.diy_s2_step2, R.string.diy_s2_step3, R.string.diy_s2_step4,
                R.string.diy_s2_step5, R.string.diy_s2_step6, R.string.diy_s2_step7, R.string.diy_s2_step8,
                R.string.diy_s2_step9, R.string.diy_s2_step10
            ),
            altTextsRes = listOf(
                R.string.diy_s2_alt1, R.string.diy_s2_alt2, R.string.diy_s2_alt3, R.string.diy_s2_alt4,
                R.string.diy_s2_alt5, R.string.diy_s2_alt6, R.string.diy_s2_alt7, R.string.diy_s2_alt8,
                R.string.diy_s2_alt9, R.string.diy_s2_alt10
            )
        ),
        DiyProjectDetailsData(
            id = "shelter_3",
            titleRes = R.string.diy_s3_title,
            category = "shelter",
            difficultyRes = R.string.difficulty_easy,
            costRes = R.string.cost_3,
            timeRes = R.string.time_15m,
            materialsRes = listOf(
                R.string.mat_styrofoam_cooler, R.string.mat_utility_knife, R.string.mat_straw,
                R.string.mat_stones, R.string.mat_decorative_tape
            ),
            descriptionRes = R.string.diy_s3_desc,
            stepsRes = listOf(
                R.string.diy_s3_step1, R.string.diy_s3_step2, R.string.diy_s3_step3, R.string.diy_s3_step4,
                R.string.diy_s3_step5, R.string.diy_s3_step6, R.string.diy_s3_step7, R.string.diy_s3_step8,
                R.string.diy_s3_step9, R.string.diy_s3_step10
            ),
            altTextsRes = listOf(
                R.string.diy_s3_alt1, R.string.diy_s3_alt2, R.string.diy_s3_alt3, R.string.diy_s3_alt4,
                R.string.diy_s3_alt5, R.string.diy_s3_alt6, R.string.diy_s3_alt7, R.string.diy_s3_alt8,
                R.string.diy_s3_alt9, R.string.diy_s3_alt10
            )
        ),
        DiyProjectDetailsData(
            id = "shelter_4",
            titleRes = R.string.diy_s4_title,
            category = "shelter",
            difficultyRes = R.string.difficulty_medium,
            costRes = R.string.cost_5,
            timeRes = R.string.time_20m,
            materialsRes = listOf(
                R.string.mat_plastic_bin, R.string.mat_utility_knife, R.string.mat_straw,
                R.string.mat_duct_tape, R.string.mat_stones
            ),
            descriptionRes = R.string.diy_s4_desc,
            stepsRes = listOf(
                R.string.diy_s4_step1, R.string.diy_s4_step2, R.string.diy_s4_step3, R.string.diy_s4_step4,
                R.string.diy_s4_step5, R.string.diy_s4_step6, R.string.diy_s4_step7, R.string.diy_s4_step8,
                R.string.diy_s4_step9, R.string.diy_s4_step10
            ),
            altTextsRes = listOf(
                R.string.diy_s4_alt1, R.string.diy_s4_alt2, R.string.diy_s4_alt3, R.string.diy_s4_alt4,
                R.string.diy_s4_alt5, R.string.diy_s4_alt6, R.string.diy_s4_alt7, R.string.diy_s4_alt8,
                R.string.diy_s4_alt9, R.string.diy_s4_alt10
            )
        ),
        DiyProjectDetailsData(
            id = "shelter_5",
            titleRes = R.string.diy_s5_title,
            category = "shelter",
            difficultyRes = R.string.difficulty_medium,
            costRes = R.string.cost_3,
            timeRes = R.string.time_20m,
            materialsRes = listOf(R.string.mat_wooden_pallet, R.string.mat_straw, R.string.mat_stones),
            descriptionRes = R.string.diy_s5_desc,
            stepsRes = sharedSteps,
            altTextsRes = sharedAlts
        ),
        DiyProjectDetailsData(
            id = "shelter_6",
            titleRes = R.string.diy_s6_title,
            category = "shelter",
            difficultyRes = R.string.difficulty_easy,
            costRes = R.string.cost_2,
            timeRes = R.string.time_15m,
            materialsRes = listOf(R.string.mat_straw_bales, R.string.mat_trash_bags),
            descriptionRes = R.string.diy_s6_desc,
            stepsRes = sharedSteps,
            altTextsRes = sharedAlts
        ),
        DiyProjectDetailsData(
            id = "shelter_7",
            titleRes = R.string.diy_s7_title,
            category = "shelter",
            difficultyRes = R.string.difficulty_hard,
            costRes = R.string.cost_5,
            timeRes = R.string.time_25m,
            materialsRes = listOf(R.string.mat_plastic_bin, R.string.mat_utility_knife, R.string.mat_duct_tape),
            descriptionRes = R.string.diy_s7_desc,
            stepsRes = sharedSteps,
            altTextsRes = sharedAlts
        ),
        DiyProjectDetailsData(
            id = "shelter_8",
            titleRes = R.string.diy_s8_title,
            category = "shelter",
            difficultyRes = R.string.difficulty_easy,
            costRes = R.string.cost_1,
            timeRes = R.string.time_10m,
            materialsRes = listOf(R.string.mat_cardboard_box, R.string.mat_straw, R.string.mat_duct_tape),
            descriptionRes = R.string.diy_s8_desc,
            stepsRes = sharedSteps,
            altTextsRes = sharedAlts
        ),
        DiyProjectDetailsData(
            id = "shelter_9",
            titleRes = R.string.diy_s9_title,
            category = "shelter",
            difficultyRes = R.string.difficulty_medium,
            costRes = R.string.cost_2,
            timeRes = R.string.time_15m,
            materialsRes = listOf(R.string.mat_cardboard_box, R.string.mat_straw, R.string.mat_utility_knife),
            descriptionRes = R.string.diy_s9_desc,
            stepsRes = sharedSteps,
            altTextsRes = sharedAlts
        ),
        DiyProjectDetailsData(
            id = "shelter_10",
            titleRes = R.string.diy_s10_title,
            category = "shelter",
            difficultyRes = R.string.difficulty_hard,
            costRes = R.string.cost_5,
            timeRes = R.string.time_25m,
            materialsRes = listOf(R.string.mat_wooden_pallet, R.string.mat_glue, R.string.mat_utility_knife),
            descriptionRes = R.string.diy_s10_desc,
            stepsRes = sharedSteps,
            altTextsRes = sharedAlts
        ),

        // ==================== GAME (10 Projects) ====================
        DiyProjectDetailsData(
            id = "game_1",
            titleRes = R.string.diy_g1_title,
            category = "game",
            difficultyRes = R.string.difficulty_easiest,
            costRes = R.string.cost_1,
            timeRes = R.string.time_10m,
            materialsRes = listOf(
                R.string.mat_feathers, R.string.mat_stick, R.string.mat_sisal_twine, R.string.mat_glue
            ),
            descriptionRes = R.string.diy_g1_desc,
            stepsRes = listOf(
                R.string.diy_g1_step1, R.string.diy_g1_step2, R.string.diy_g1_step3, R.string.diy_g1_step4,
                R.string.diy_g1_step5, R.string.diy_g1_step6, R.string.diy_g1_step7, R.string.diy_g1_step8,
                R.string.diy_g1_step9, R.string.diy_g1_step10
            ),
            altTextsRes = listOf(
                R.string.diy_g1_alt1, R.string.diy_g1_alt2, R.string.diy_g1_alt3, R.string.diy_g1_alt4,
                R.string.diy_g1_alt5, R.string.diy_g1_alt6, R.string.diy_g1_alt7, R.string.diy_g1_alt8,
                R.string.diy_g1_alt9, R.string.diy_g1_alt10
            )
        ),
        DiyProjectDetailsData(
            id = "game_2",
            titleRes = R.string.diy_g2_title,
            category = "game",
            difficultyRes = R.string.difficulty_easiest,
            costRes = R.string.cost_0,
            timeRes = R.string.time_5m,
            materialsRes = listOf(R.string.mat_newspaper, R.string.mat_scissors),
            descriptionRes = R.string.diy_g2_desc,
            stepsRes = sharedSteps,
            altTextsRes = sharedAlts
        ),
        DiyProjectDetailsData(
            id = "game_3",
            titleRes = R.string.diy_g3_title,
            category = "game",
            difficultyRes = R.string.difficulty_medium,
            costRes = R.string.cost_1,
            timeRes = R.string.time_15m,
            materialsRes = listOf(R.string.mat_cardboard_box, R.string.mat_scissors, R.string.mat_glue),
            descriptionRes = R.string.diy_g3_desc,
            stepsRes = sharedSteps,
            altTextsRes = sharedAlts
        ),
        DiyProjectDetailsData(
            id = "game_4",
            titleRes = R.string.diy_g4_title,
            category = "game",
            difficultyRes = R.string.difficulty_easy,
            costRes = R.string.cost_0,
            timeRes = R.string.time_10m,
            materialsRes = listOf(R.string.mat_utility_knife),
            descriptionRes = R.string.diy_g4_desc,
            stepsRes = sharedSteps,
            altTextsRes = sharedAlts
        ),
        DiyProjectDetailsData(
            id = "game_5",
            titleRes = R.string.diy_g5_title,
            category = "game",
            difficultyRes = R.string.difficulty_easy,
            costRes = R.string.cost_2,
            timeRes = R.string.time_15m,
            materialsRes = listOf(R.string.mat_sisal_twine, R.string.mat_cardboard_box, R.string.mat_glue),
            descriptionRes = R.string.diy_g5_desc,
            stepsRes = sharedSteps,
            altTextsRes = sharedAlts
        ),
        DiyProjectDetailsData(
            id = "game_6",
            titleRes = R.string.diy_g6_title,
            category = "game",
            difficultyRes = R.string.difficulty_easiest,
            costRes = R.string.cost_0,
            timeRes = R.string.time_5m,
            materialsRes = listOf(R.string.mat_scissors),
            descriptionRes = R.string.diy_g6_desc,
            stepsRes = sharedSteps,
            altTextsRes = sharedAlts
        ),
        DiyProjectDetailsData(
            id = "game_7",
            titleRes = R.string.diy_g7_title,
            category = "game",
            difficultyRes = R.string.difficulty_easy,
            costRes = R.string.cost_1,
            timeRes = R.string.time_10m,
            materialsRes = listOf(R.string.mat_sisal_twine, R.string.mat_scissors, R.string.mat_bell),
            descriptionRes = R.string.diy_g7_desc,
            stepsRes = sharedSteps,
            altTextsRes = sharedAlts
        ),
        DiyProjectDetailsData(
            id = "game_8",
            titleRes = R.string.diy_g8_title,
            category = "game",
            difficultyRes = R.string.difficulty_easy,
            costRes = R.string.cost_1,
            timeRes = R.string.time_10m,
            materialsRes = listOf(R.string.mat_wool_sweater, R.string.mat_scissors),
            descriptionRes = R.string.diy_g8_desc,
            stepsRes = sharedSteps,
            altTextsRes = sharedAlts
        ),
        DiyProjectDetailsData(
            id = "game_9",
            titleRes = R.string.diy_g9_title,
            category = "game",
            difficultyRes = R.string.difficulty_easy,
            costRes = R.string.cost_1,
            timeRes = R.string.time_10m,
            materialsRes = listOf(R.string.mat_stick, R.string.mat_fleece_fabric, R.string.mat_glue),
            descriptionRes = R.string.diy_g9_desc,
            stepsRes = sharedSteps,
            altTextsRes = sharedAlts
        ),
        DiyProjectDetailsData(
            id = "game_10",
            titleRes = R.string.diy_g10_title,
            category = "game",
            difficultyRes = R.string.difficulty_medium,
            costRes = R.string.cost_2,
            timeRes = R.string.time_20m,
            materialsRes = listOf(R.string.mat_cardboard_box, R.string.mat_utility_knife, R.string.mat_stick),
            descriptionRes = R.string.diy_g10_desc,
            stepsRes = sharedSteps,
            altTextsRes = sharedAlts
        ),

        // ==================== COZY (10 Projects) ====================
        DiyProjectDetailsData(
            id = "cozy_1",
            titleRes = R.string.diy_c1_title,
            category = "cozy",
            difficultyRes = R.string.difficulty_easy,
            costRes = R.string.cost_0,
            timeRes = R.string.time_15m,
            materialsRes = listOf(
                R.string.mat_newspaper, R.string.mat_flat_ground, R.string.mat_cardboard_box, R.string.mat_scissors
            ),
            descriptionRes = R.string.diy_c1_desc,
            stepsRes = listOf(
                R.string.diy_c1_step1, R.string.diy_c1_step2, R.string.diy_c1_step3, R.string.diy_c1_step4,
                R.string.diy_c1_step5, R.string.diy_c1_step6, R.string.diy_c1_step7, R.string.diy_c1_step8,
                R.string.diy_c1_step9, R.string.diy_c1_step10
            ),
            altTextsRes = listOf(
                R.string.diy_c1_alt1, R.string.diy_c1_alt2, R.string.diy_c1_alt3, R.string.diy_c1_alt4,
                R.string.diy_c1_alt5, R.string.diy_c1_alt6, R.string.diy_c1_alt7, R.string.diy_c1_alt8,
                R.string.diy_c1_alt9, R.string.diy_c1_alt10
            )
        ),
        DiyProjectDetailsData(
            id = "cozy_2",
            titleRes = R.string.diy_c2_title,
            category = "cozy",
            difficultyRes = R.string.difficulty_medium,
            costRes = R.string.cost_3,
            timeRes = R.string.time_20m,
            materialsRes = listOf(R.string.mat_felt, R.string.mat_scissors, R.string.mat_glue),
            descriptionRes = R.string.diy_c2_desc,
            stepsRes = sharedSteps,
            altTextsRes = sharedAlts
        ),
        DiyProjectDetailsData(
            id = "cozy_3",
            titleRes = R.string.diy_c3_title,
            category = "cozy",
            difficultyRes = R.string.difficulty_medium,
            costRes = R.string.cost_3,
            timeRes = R.string.time_25m,
            materialsRes = listOf(R.string.mat_fleece_fabric, R.string.mat_sisal_twine, R.string.mat_stick),
            descriptionRes = R.string.diy_c3_desc,
            stepsRes = sharedSteps,
            altTextsRes = sharedAlts
        ),
        DiyProjectDetailsData(
            id = "cozy_4",
            titleRes = R.string.diy_c4_title,
            category = "cozy",
            difficultyRes = R.string.difficulty_easy,
            costRes = R.string.cost_1,
            timeRes = R.string.time_15m,
            materialsRes = listOf(R.string.mat_wool_sweater, R.string.mat_scissors),
            descriptionRes = R.string.diy_c4_desc,
            stepsRes = sharedSteps,
            altTextsRes = sharedAlts
        ),
        DiyProjectDetailsData(
            id = "cozy_5",
            titleRes = R.string.diy_c5_title,
            category = "cozy",
            difficultyRes = R.string.difficulty_hard,
            costRes = R.string.cost_5,
            timeRes = R.string.time_25m,
            materialsRes = listOf(R.string.mat_foam, R.string.mat_fleece_fabric, R.string.mat_glue),
            descriptionRes = R.string.diy_c5_desc,
            stepsRes = sharedSteps,
            altTextsRes = sharedAlts
        ),
        DiyProjectDetailsData(
            id = "cozy_6",
            titleRes = R.string.diy_c6_title,
            category = "cozy",
            difficultyRes = R.string.difficulty_medium,
            costRes = R.string.cost_3,
            timeRes = R.string.time_20m,
            materialsRes = listOf(R.string.mat_cardboard_box, R.string.mat_trash_bags, R.string.mat_duct_tape),
            descriptionRes = R.string.diy_c6_desc,
            stepsRes = sharedSteps,
            altTextsRes = sharedAlts
        ),
        DiyProjectDetailsData(
            id = "cozy_7",
            titleRes = R.string.diy_c7_title,
            category = "cozy",
            difficultyRes = R.string.difficulty_hard,
            costRes = R.string.cost_5,
            timeRes = R.string.time_25m,
            materialsRes = listOf(R.string.mat_stick, R.string.mat_fleece_fabric, R.string.mat_sisal_twine),
            descriptionRes = R.string.diy_c7_desc,
            stepsRes = sharedSteps,
            altTextsRes = sharedAlts
        ),
        DiyProjectDetailsData(
            id = "cozy_8",
            titleRes = R.string.diy_c8_title,
            category = "cozy",
            difficultyRes = R.string.difficulty_easy,
            costRes = R.string.cost_2,
            timeRes = R.string.time_12m,
            materialsRes = listOf(R.string.mat_fleece_fabric, R.string.mat_sisal_twine),
            descriptionRes = R.string.diy_c8_desc,
            stepsRes = sharedSteps,
            altTextsRes = sharedAlts
        ),
        DiyProjectDetailsData(
            id = "cozy_9",
            titleRes = R.string.diy_c9_title,
            category = "cozy",
            difficultyRes = R.string.difficulty_easy,
            costRes = R.string.cost_2,
            timeRes = R.string.time_15m,
            materialsRes = listOf(R.string.mat_fruit_crate, R.string.mat_wool_sweater),
            descriptionRes = R.string.diy_c9_desc,
            stepsRes = sharedSteps,
            altTextsRes = sharedAlts
        ),
        DiyProjectDetailsData(
            id = "cozy_10",
            titleRes = R.string.diy_c10_title,
            category = "cozy",
            difficultyRes = R.string.difficulty_hard,
            costRes = R.string.cost_5,
            timeRes = R.string.time_25m,
            materialsRes = listOf(R.string.mat_velvet, R.string.mat_foam, R.string.mat_glue),
            descriptionRes = R.string.diy_c10_desc,
            stepsRes = sharedSteps,
            altTextsRes = sharedAlts
        )
    )
}
