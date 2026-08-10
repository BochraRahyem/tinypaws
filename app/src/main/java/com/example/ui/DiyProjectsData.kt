package com.example.ui

import com.example.R

data class DiyStepData(
    val step: Int,
    val titleRes: Int,
    val actionId: String
)

data class ActionLibraryItemData(
    val id: String,
    val imagePrompt: String
)

data class DiyProjectDetailsData(
    val id: String,
    val titleRes: Int,
    val categoryRes: Int,
    val difficultyRes: Int,
    val costRes: Int,
    val timeRes: Int,
    val descriptionRes: Int,
    val guideParagraphRes: Int,
    val steps: List<DiyStepData>
)

object DiyProjectsData {
    val actionLibrary: Map<String, ActionLibraryItemData> = mapOf(
        "gather_materials" to ActionLibraryItemData("gather_materials", "Girl laying out craft materials for a DIY project on a table or floor, Pip sniffing at the pile curiously"),
        "measure" to ActionLibraryItemData("measure", "Girl measuring materials with a tape measure, focused expression, Pip watching beside her"),
        "plan_sketch" to ActionLibraryItemData("plan_sketch", "Girl sketching a simple plan or design on paper with a pencil, Pip sitting on the desk nearby"),
        "cut_opening" to ActionLibraryItemData("cut_opening", "Girl carefully cutting a doorway or opening into a box/material with scissors or a craft knife, Pip peeking around the corner"),
        "cut_to_size" to ActionLibraryItemData("cut_to_size", "Girl cutting a material (fabric, wood, or cardboard) into a shape on a flat surface, offcuts scattered nearby"),
        "insulate_line" to ActionLibraryItemData("insulate_line", "Girl lining the inside of a structure with soft insulating material, pressing it into place"),
        "add_straw" to ActionLibraryItemData("add_straw", "Girl scooping straw or soft bedding material into the base of a structure, Pip curling into the pile"),
        "sand_edges" to ActionLibraryItemData("sand_edges", "Girl sanding a rough edge of wood or cardboard smooth with sandpaper"),
        "build_frame" to ActionLibraryItemData("build_frame", "Girl assembling a basic frame or structure from panels or parts, focused and hands-on"),
        "attach_join" to ActionLibraryItemData("attach_join", "Girl attaching, screwing, or taping two pieces of a project together"),
        "secure_lid" to ActionLibraryItemData("secure_lid", "Girl pressing a lid or roof piece firmly onto a finished structure"),
        "waterproof_cover" to ActionLibraryItemData("waterproof_cover", "Girl draping and securing a tarp or protective cover over a finished structure"),
        "elevate_place" to ActionLibraryItemData("elevate_place", "Girl setting a finished structure onto a raised base like bricks or a small pallet"),
        "sew_edge" to ActionLibraryItemData("sew_edge", "Girl hand-sewing along a fabric edge with a needle and thread, focused and careful"),
        "stuff_fill" to ActionLibraryItemData("stuff_fill", "Girl stuffing soft filling material into a fabric shape, fluffy filling visible"),
        "tie_knot" to ActionLibraryItemData("tie_knot", "Girl tying a string, rope, or ribbon into a secure knot on a craft project"),
        "fold_shape" to ActionLibraryItemData("fold_shape", "Girl folding fabric or soft material into a rounded shape with her hands"),
        "paint_decorate" to ActionLibraryItemData("paint_decorate", "Girl painting, sealing, or decorating the outside of a finished project with a small brush"),
        "glue_pieces" to ActionLibraryItemData("glue_pieces", "Girl gluing two craft pieces together, holding them steady while the glue sets"),
        "quality_check" to ActionLibraryItemData("quality_check", "Girl closely inspecting a finished piece in her hands, checking for rough or unsafe spots"),
        "test_play" to ActionLibraryItemData("test_play", "Girl watching happily as Pip plays with, climbs into, or tests out the finished project"),
        "final_placement" to ActionLibraryItemData("final_placement", "Girl setting the finished project into its cozy final spot, Pip walking toward it")
    )

    private const val ART_STYLE_PREFIX = "Soft pastel children's storybook illustration in cartoon style, cozy muted palette, warm lighting, high detail, soft brush texture. A young girl with two hair buns wearing a cozy beige knit sweater sitting in a warm craft room with shelves of pastel yarn and plants, next to a small orange cat named Pip. "

    fun getActionPrompt(actionId: String): String {
        val item = actionLibrary[actionId] ?: return ART_STYLE_PREFIX + "Girl doing DIY craft with Pip the cat"
        return ART_STYLE_PREFIX + item.imagePrompt
    }

    val projects: List<DiyProjectDetailsData> = listOf(
        // ==================== SHELTER (10 Projects) ====================
        DiyProjectDetailsData(
            id = "sh_e1",
            titleRes = R.string.diy_sh_e1_title,
            categoryRes = R.string.cat_shelter,
            difficultyRes = R.string.difficulty_easy,
            costRes = R.string.cost_free,
            timeRes = R.string.time_5m,
            descriptionRes = R.string.diy_sh_e1_desc,
            guideParagraphRes = R.string.diy_sh_e1_guide,
            steps = listOf(
                DiyStepData(1, R.string.diy_sh_e1_step1, "gather_materials"),
                DiyStepData(2, R.string.diy_sh_e1_step2, "cut_opening"),
                DiyStepData(3, R.string.diy_sh_e1_step3, "final_placement")
            )
        ),
        DiyProjectDetailsData(
            id = "sh_e2",
            titleRes = R.string.diy_sh_e2_title,
            categoryRes = R.string.cat_shelter,
            difficultyRes = R.string.difficulty_easy,
            costRes = R.string.cost_free,
            timeRes = R.string.time_10m,
            descriptionRes = R.string.diy_sh_e2_desc,
            guideParagraphRes = R.string.diy_sh_e2_guide,
            steps = listOf(
                DiyStepData(1, R.string.diy_sh_e2_step1, "gather_materials"),
                DiyStepData(2, R.string.diy_sh_e2_step2, "build_frame"),
                DiyStepData(3, R.string.diy_sh_e2_step3, "final_placement")
            )
        ),
        DiyProjectDetailsData(
            id = "sh_e3",
            titleRes = R.string.diy_sh_e3_title,
            categoryRes = R.string.cat_shelter,
            difficultyRes = R.string.difficulty_easy,
            costRes = R.string.cost_2d,
            timeRes = R.string.time_15m,
            descriptionRes = R.string.diy_sh_e3_desc,
            guideParagraphRes = R.string.diy_sh_e3_guide,
            steps = listOf(
                DiyStepData(1, R.string.diy_sh_e3_step1, "gather_materials"),
                DiyStepData(2, R.string.diy_sh_e3_step2, "insulate_line"),
                DiyStepData(3, R.string.diy_sh_e3_step3, "cut_opening"),
                DiyStepData(4, R.string.diy_sh_e3_step4, "waterproof_cover")
            )
        ),
        DiyProjectDetailsData(
            id = "sh_e4",
            titleRes = R.string.diy_sh_e4_title,
            categoryRes = R.string.cat_shelter,
            difficultyRes = R.string.difficulty_easy,
            costRes = R.string.cost_3d,
            timeRes = R.string.time_15m,
            descriptionRes = R.string.diy_sh_e4_desc,
            guideParagraphRes = R.string.diy_sh_e4_guide,
            steps = listOf(
                DiyStepData(1, R.string.diy_sh_e4_step1, "gather_materials"),
                DiyStepData(2, R.string.diy_sh_e4_step2, "cut_opening"),
                DiyStepData(3, R.string.diy_sh_e4_step3, "add_straw"),
                DiyStepData(4, R.string.diy_sh_e4_step4, "secure_lid")
            )
        ),
        DiyProjectDetailsData(
            id = "sh_m1",
            titleRes = R.string.diy_sh_m1_title,
            categoryRes = R.string.cat_shelter,
            difficultyRes = R.string.difficulty_medium,
            costRes = R.string.cost_5d,
            timeRes = R.string.time_20m,
            descriptionRes = R.string.diy_sh_m1_desc,
            guideParagraphRes = R.string.diy_sh_m1_guide,
            steps = listOf(
                DiyStepData(1, R.string.diy_sh_m1_step1, "gather_materials"),
                DiyStepData(2, R.string.diy_sh_m1_step2, "measure"),
                DiyStepData(3, R.string.diy_sh_m1_step3, "cut_opening"),
                DiyStepData(4, R.string.diy_sh_m1_step4, "insulate_line"),
                DiyStepData(5, R.string.diy_sh_m1_step5, "add_straw"),
                DiyStepData(6, R.string.diy_sh_m1_step6, "final_placement")
            )
        ),
        DiyProjectDetailsData(
            id = "sh_m2",
            titleRes = R.string.diy_sh_m2_title,
            categoryRes = R.string.cat_shelter,
            difficultyRes = R.string.difficulty_medium,
            costRes = R.string.cost_free,
            timeRes = R.string.time_20m,
            descriptionRes = R.string.diy_sh_m2_desc,
            guideParagraphRes = R.string.diy_sh_m2_guide,
            steps = listOf(
                DiyStepData(1, R.string.diy_sh_m2_step1, "gather_materials"),
                DiyStepData(2, R.string.diy_sh_m2_step2, "sand_edges"),
                DiyStepData(3, R.string.diy_sh_m2_step3, "build_frame"),
                DiyStepData(4, R.string.diy_sh_m2_step4, "attach_join"),
                DiyStepData(5, R.string.diy_sh_m2_step5, "add_straw"),
                DiyStepData(6, R.string.diy_sh_m2_step6, "waterproof_cover")
            )
        ),
        DiyProjectDetailsData(
            id = "sh_m3",
            titleRes = R.string.diy_sh_m3_title,
            categoryRes = R.string.cat_shelter,
            difficultyRes = R.string.difficulty_medium,
            costRes = R.string.cost_8d,
            timeRes = R.string.time_25m,
            descriptionRes = R.string.diy_sh_m3_desc,
            guideParagraphRes = R.string.diy_sh_m3_guide,
            steps = listOf(
                DiyStepData(1, R.string.diy_sh_m3_step1, "gather_materials"),
                DiyStepData(2, R.string.diy_sh_m3_step2, "cut_opening"),
                DiyStepData(3, R.string.diy_sh_m3_step3, "attach_join"),
                DiyStepData(4, R.string.diy_sh_m3_step4, "cut_opening"),
                DiyStepData(5, R.string.diy_sh_m3_step5, "insulate_line"),
                DiyStepData(6, R.string.diy_sh_m3_step6, "add_straw"),
                DiyStepData(7, R.string.diy_sh_m3_step7, "waterproof_cover")
            )
        ),
        DiyProjectDetailsData(
            id = "sh_h1",
            titleRes = R.string.diy_sh_h1_title,
            categoryRes = R.string.cat_shelter,
            difficultyRes = R.string.difficulty_hard,
            costRes = R.string.cost_15d,
            timeRes = R.string.time_45m,
            descriptionRes = R.string.diy_sh_h1_desc,
            guideParagraphRes = R.string.diy_sh_h1_guide,
            steps = listOf(
                DiyStepData(1, R.string.diy_sh_h1_step1, "gather_materials"),
                DiyStepData(2, R.string.diy_sh_h1_step2, "cut_to_size"),
                DiyStepData(3, R.string.diy_sh_h1_step3, "build_frame"),
                DiyStepData(4, R.string.diy_sh_h1_step4, "cut_opening"),
                DiyStepData(5, R.string.diy_sh_h1_step5, "insulate_line"),
                DiyStepData(6, R.string.diy_sh_h1_step6, "attach_join"),
                DiyStepData(7, R.string.diy_sh_h1_step7, "paint_decorate"),
                DiyStepData(8, R.string.diy_sh_h1_step8, "elevate_place")
            )
        ),
        DiyProjectDetailsData(
            id = "sh_h2",
            titleRes = R.string.diy_sh_h2_title,
            categoryRes = R.string.cat_shelter,
            difficultyRes = R.string.difficulty_hard,
            costRes = R.string.cost_12d,
            timeRes = R.string.time_40m,
            descriptionRes = R.string.diy_sh_h2_desc,
            guideParagraphRes = R.string.diy_sh_h2_guide,
            steps = listOf(
                DiyStepData(1, R.string.diy_sh_h2_step1, "gather_materials"),
                DiyStepData(2, R.string.diy_sh_h2_step2, "build_frame"),
                DiyStepData(3, R.string.diy_sh_h2_step3, "insulate_line"),
                DiyStepData(4, R.string.diy_sh_h2_step4, "insulate_line"),
                DiyStepData(5, R.string.diy_sh_h2_step5, "build_frame"),
                DiyStepData(6, R.string.diy_sh_h2_step6, "cut_opening"),
                DiyStepData(7, R.string.diy_sh_h2_step7, "add_straw"),
                DiyStepData(8, R.string.diy_sh_h2_step8, "final_placement")
            )
        ),
        DiyProjectDetailsData(
            id = "sh_h3",
            titleRes = R.string.diy_sh_h3_title,
            categoryRes = R.string.cat_shelter,
            difficultyRes = R.string.difficulty_hard,
            costRes = R.string.cost_20d,
            timeRes = R.string.time_60m,
            descriptionRes = R.string.diy_sh_h3_desc,
            guideParagraphRes = R.string.diy_sh_h3_guide,
            steps = listOf(
                DiyStepData(1, R.string.diy_sh_h3_step1, "plan_sketch"),
                DiyStepData(2, R.string.diy_sh_h3_step2, "gather_materials"),
                DiyStepData(3, R.string.diy_sh_h3_step3, "cut_opening"),
                DiyStepData(4, R.string.diy_sh_h3_step4, "insulate_line"),
                DiyStepData(5, R.string.diy_sh_h3_step5, "elevate_place"),
                DiyStepData(6, R.string.diy_sh_h3_step6, "add_straw"),
                DiyStepData(7, R.string.diy_sh_h3_step7, "attach_join"),
                DiyStepData(8, R.string.diy_sh_h3_step8, "waterproof_cover"),
                DiyStepData(9, R.string.diy_sh_h3_step9, "final_placement")
            )
        ),

        // ==================== GAME (10 Projects) ====================
        DiyProjectDetailsData(
            id = "gm_e1",
            titleRes = R.string.diy_gm_e1_title,
            categoryRes = R.string.cat_game,
            difficultyRes = R.string.difficulty_easy,
            costRes = R.string.cost_free,
            timeRes = R.string.time_5m,
            descriptionRes = R.string.diy_gm_e1_desc,
            guideParagraphRes = R.string.diy_gm_e1_guide,
            steps = listOf(
                DiyStepData(1, R.string.diy_gm_e1_step1, "gather_materials"),
                DiyStepData(2, R.string.diy_gm_e1_step2, "stuff_fill"),
                DiyStepData(3, R.string.diy_gm_e1_step3, "tie_knot")
            )
        ),
        DiyProjectDetailsData(
            id = "gm_e2",
            titleRes = R.string.diy_gm_e2_title,
            categoryRes = R.string.cat_game,
            difficultyRes = R.string.difficulty_easy,
            costRes = R.string.cost_free,
            timeRes = R.string.time_3m,
            descriptionRes = R.string.diy_gm_e2_desc,
            guideParagraphRes = R.string.diy_gm_e2_guide,
            steps = listOf(
                DiyStepData(1, R.string.diy_gm_e2_step1, "gather_materials"),
                DiyStepData(2, R.string.diy_gm_e2_step2, "cut_opening"),
                DiyStepData(3, R.string.diy_gm_e2_step3, "test_play")
            )
        ),
        DiyProjectDetailsData(
            id = "gm_e3",
            titleRes = R.string.diy_gm_e3_title,
            categoryRes = R.string.cat_game,
            difficultyRes = R.string.difficulty_easy,
            costRes = R.string.cost_free,
            timeRes = R.string.time_5m,
            descriptionRes = R.string.diy_gm_e3_desc,
            guideParagraphRes = R.string.diy_gm_e3_guide,
            steps = listOf(
                DiyStepData(1, R.string.diy_gm_e3_step1, "gather_materials"),
                DiyStepData(2, R.string.diy_gm_e3_step2, "fold_shape"),
                DiyStepData(3, R.string.diy_gm_e3_step3, "stuff_fill"),
                DiyStepData(4, R.string.diy_gm_e3_step4, "final_placement")
            )
        ),
        DiyProjectDetailsData(
            id = "gm_e4",
            titleRes = R.string.diy_gm_e4_title,
            categoryRes = R.string.cat_game,
            difficultyRes = R.string.difficulty_easy,
            costRes = R.string.cost_free,
            timeRes = R.string.time_5m,
            descriptionRes = R.string.diy_gm_e4_desc,
            guideParagraphRes = R.string.diy_gm_e4_guide,
            steps = listOf(
                DiyStepData(1, R.string.diy_gm_e4_step1, "gather_materials"),
                DiyStepData(2, R.string.diy_gm_e4_step2, "quality_check"),
                DiyStepData(3, R.string.diy_gm_e4_step3, "attach_join"),
                DiyStepData(4, R.string.diy_gm_e4_step4, "test_play")
            )
        ),
        DiyProjectDetailsData(
            id = "gm_m1",
            titleRes = R.string.diy_gm_m1_title,
            categoryRes = R.string.cat_game,
            difficultyRes = R.string.difficulty_medium,
            costRes = R.string.cost_1d,
            timeRes = R.string.time_15m,
            descriptionRes = R.string.diy_gm_m1_desc,
            guideParagraphRes = R.string.diy_gm_m1_guide,
            steps = listOf(
                DiyStepData(1, R.string.diy_gm_m1_step1, "gather_materials"),
                DiyStepData(2, R.string.diy_gm_m1_step2, "plan_sketch"),
                DiyStepData(3, R.string.diy_gm_m1_step3, "cut_to_size"),
                DiyStepData(4, R.string.diy_gm_m1_step4, "glue_pieces"),
                DiyStepData(5, R.string.diy_gm_m1_step5, "cut_opening"),
                DiyStepData(6, R.string.diy_gm_m1_step6, "test_play")
            )
        ),
        DiyProjectDetailsData(
            id = "gm_m2",
            titleRes = R.string.diy_gm_m2_title,
            categoryRes = R.string.cat_game,
            difficultyRes = R.string.difficulty_medium,
            costRes = R.string.cost_2d,
            timeRes = R.string.time_12m,
            descriptionRes = R.string.diy_gm_m2_desc,
            guideParagraphRes = R.string.diy_gm_m2_guide,
            steps = listOf(
                DiyStepData(1, R.string.diy_gm_m2_step1, "gather_materials"),
                DiyStepData(2, R.string.diy_gm_m2_step2, "tie_knot"),
                DiyStepData(3, R.string.diy_gm_m2_step3, "tie_knot"),
                DiyStepData(4, R.string.diy_gm_m2_step4, "attach_join"),
                DiyStepData(5, R.string.diy_gm_m2_step5, "test_play")
            )
        ),
        DiyProjectDetailsData(
            id = "gm_m3",
            titleRes = R.string.diy_gm_m3_title,
            categoryRes = R.string.cat_game,
            difficultyRes = R.string.difficulty_medium,
            costRes = R.string.cost_2d,
            timeRes = R.string.time_15m,
            descriptionRes = R.string.diy_gm_m3_desc,
            guideParagraphRes = R.string.diy_gm_m3_guide,
            steps = listOf(
                DiyStepData(1, R.string.diy_gm_m3_step1, "cut_to_size"),
                DiyStepData(2, R.string.diy_gm_m3_step2, "attach_join"),
                DiyStepData(3, R.string.diy_gm_m3_step3, "sew_edge"),
                DiyStepData(4, R.string.diy_gm_m3_step4, "fold_shape"),
                DiyStepData(5, R.string.diy_gm_m3_step5, "stuff_fill"),
                DiyStepData(6, R.string.diy_gm_m3_step6, "sew_edge")
            )
        ),
        DiyProjectDetailsData(
            id = "gm_h1",
            titleRes = R.string.diy_gm_h1_title,
            categoryRes = R.string.cat_game,
            difficultyRes = R.string.difficulty_hard,
            costRes = R.string.cost_5d,
            timeRes = R.string.time_30m,
            descriptionRes = R.string.diy_gm_h1_desc,
            guideParagraphRes = R.string.diy_gm_h1_guide,
            steps = listOf(
                DiyStepData(1, R.string.diy_gm_h1_step1, "gather_materials"),
                DiyStepData(2, R.string.diy_gm_h1_step2, "plan_sketch"),
                DiyStepData(3, R.string.diy_gm_h1_step3, "cut_opening"),
                DiyStepData(4, R.string.diy_gm_h1_step4, "attach_join"),
                DiyStepData(5, R.string.diy_gm_h1_step5, "cut_opening"),
                DiyStepData(6, R.string.diy_gm_h1_step6, "paint_decorate"),
                DiyStepData(7, R.string.diy_gm_h1_step7, "glue_pieces"),
                DiyStepData(8, R.string.diy_gm_h1_step8, "test_play")
            )
        ),
        DiyProjectDetailsData(
            id = "gm_h2",
            titleRes = R.string.diy_gm_h2_title,
            categoryRes = R.string.cat_game,
            difficultyRes = R.string.difficulty_hard,
            costRes = R.string.cost_4d,
            timeRes = R.string.time_25m,
            descriptionRes = R.string.diy_gm_h2_desc,
            guideParagraphRes = R.string.diy_gm_h2_guide,
            steps = listOf(
                DiyStepData(1, R.string.diy_gm_h2_step1, "gather_materials"),
                DiyStepData(2, R.string.diy_gm_h2_step2, "plan_sketch"),
                DiyStepData(3, R.string.diy_gm_h2_step3, "cut_opening"),
                DiyStepData(4, R.string.diy_gm_h2_step4, "sand_edges"),
                DiyStepData(5, R.string.diy_gm_h2_step5, "glue_pieces"),
                DiyStepData(6, R.string.diy_gm_h2_step6, "stuff_fill"),
                DiyStepData(7, R.string.diy_gm_h2_step7, "test_play")
            )
        ),
        DiyProjectDetailsData(
            id = "gm_h3",
            titleRes = R.string.diy_gm_h3_title,
            categoryRes = R.string.cat_game,
            difficultyRes = R.string.difficulty_hard,
            costRes = R.string.cost_8d,
            timeRes = R.string.time_35m,
            descriptionRes = R.string.diy_gm_h3_desc,
            guideParagraphRes = R.string.diy_gm_h3_guide,
            steps = listOf(
                DiyStepData(1, R.string.diy_gm_h3_step1, "gather_materials"),
                DiyStepData(2, R.string.diy_gm_h3_step2, "attach_join"),
                DiyStepData(3, R.string.diy_gm_h3_step3, "tie_knot"),
                DiyStepData(4, R.string.diy_gm_h3_step4, "attach_join"),
                DiyStepData(5, R.string.diy_gm_h3_step5, "glue_pieces"),
                DiyStepData(6, R.string.diy_gm_h3_step6, "tie_knot"),
                DiyStepData(7, R.string.diy_gm_h3_step7, "glue_pieces"),
                DiyStepData(8, R.string.diy_gm_h3_step8, "test_play")
            )
        ),

        // ==================== COZY (10 Projects) ====================
        DiyProjectDetailsData(
            id = "bd_e1",
            titleRes = R.string.diy_bd_e1_title,
            categoryRes = R.string.cat_cozy,
            difficultyRes = R.string.difficulty_easy,
            costRes = R.string.cost_free,
            timeRes = R.string.time_10m,
            descriptionRes = R.string.diy_bd_e1_desc,
            guideParagraphRes = R.string.diy_bd_e1_guide,
            steps = listOf(
                DiyStepData(1, R.string.diy_bd_e1_step1, "gather_materials"),
                DiyStepData(2, R.string.diy_bd_e1_step2, "tie_knot"),
                DiyStepData(3, R.string.diy_bd_e1_step3, "final_placement")
            )
        ),
        DiyProjectDetailsData(
            id = "bd_e2",
            titleRes = R.string.diy_bd_e2_title,
            categoryRes = R.string.cat_cozy,
            difficultyRes = R.string.difficulty_easy,
            costRes = R.string.cost_free,
            timeRes = R.string.time_5m,
            descriptionRes = R.string.diy_bd_e2_desc,
            guideParagraphRes = R.string.diy_bd_e2_guide,
            steps = listOf(
                DiyStepData(1, R.string.diy_bd_e2_step1, "gather_materials"),
                DiyStepData(2, R.string.diy_bd_e2_step2, "fold_shape"),
                DiyStepData(3, R.string.diy_bd_e2_step3, "final_placement")
            )
        ),
        DiyProjectDetailsData(
            id = "bd_e3",
            titleRes = R.string.diy_bd_e3_title,
            categoryRes = R.string.cat_cozy,
            difficultyRes = R.string.difficulty_easy,
            costRes = R.string.cost_free,
            timeRes = R.string.time_8m,
            descriptionRes = R.string.diy_bd_e3_desc,
            guideParagraphRes = R.string.diy_bd_e3_guide,
            steps = listOf(
                DiyStepData(1, R.string.diy_bd_e3_step1, "gather_materials"),
                DiyStepData(2, R.string.diy_bd_e3_step2, "cut_opening"),
                DiyStepData(3, R.string.diy_bd_e3_step3, "insulate_line"),
                DiyStepData(4, R.string.diy_bd_e3_step4, "final_placement")
            )
        ),
        DiyProjectDetailsData(
            id = "bd_e4",
            titleRes = R.string.diy_bd_e4_title,
            categoryRes = R.string.cat_cozy,
            difficultyRes = R.string.difficulty_easy,
            costRes = R.string.cost_free,
            timeRes = R.string.time_5m,
            descriptionRes = R.string.diy_bd_e4_desc,
            guideParagraphRes = R.string.diy_bd_e4_guide,
            steps = listOf(
                DiyStepData(1, R.string.diy_bd_e4_step1, "gather_materials"),
                DiyStepData(2, R.string.diy_bd_e4_step2, "fold_shape"),
                DiyStepData(3, R.string.diy_bd_e4_step3, "tie_knot"),
                DiyStepData(4, R.string.diy_bd_e4_step4, "final_placement")
            )
        ),
        DiyProjectDetailsData(
            id = "bd_m1",
            titleRes = R.string.diy_bd_m1_title,
            categoryRes = R.string.cat_cozy,
            difficultyRes = R.string.difficulty_medium,
            costRes = R.string.cost_4d,
            timeRes = R.string.time_20m,
            descriptionRes = R.string.diy_bd_m1_desc,
            guideParagraphRes = R.string.diy_bd_m1_guide,
            steps = listOf(
                DiyStepData(1, R.string.diy_bd_m1_step1, "cut_to_size"),
                DiyStepData(2, R.string.diy_bd_m1_step2, "sew_edge"),
                DiyStepData(3, R.string.diy_bd_m1_step3, "build_frame"),
                DiyStepData(4, R.string.diy_bd_m1_step4, "sew_edge"),
                DiyStepData(5, R.string.diy_bd_m1_step5, "cut_opening"),
                DiyStepData(6, R.string.diy_bd_m1_step6, "final_placement")
            )
        ),
        DiyProjectDetailsData(
            id = "bd_m2",
            titleRes = R.string.diy_bd_m2_title,
            categoryRes = R.string.cat_cozy,
            difficultyRes = R.string.difficulty_medium,
            costRes = R.string.cost_3d,
            timeRes = R.string.time_18m,
            descriptionRes = R.string.diy_bd_m2_desc,
            guideParagraphRes = R.string.diy_bd_m2_guide,
            steps = listOf(
                DiyStepData(1, R.string.diy_bd_m2_step1, "sew_edge"),
                DiyStepData(2, R.string.diy_bd_m2_step2, "stuff_fill"),
                DiyStepData(3, R.string.diy_bd_m2_step3, "sew_edge"),
                DiyStepData(4, R.string.diy_bd_m2_step4, "sew_edge"),
                DiyStepData(5, R.string.diy_bd_m2_step5, "final_placement")
            )
        ),
        DiyProjectDetailsData(
            id = "bd_m3",
            titleRes = R.string.diy_bd_m3_title,
            categoryRes = R.string.cat_cozy,
            difficultyRes = R.string.difficulty_medium,
            costRes = R.string.cost_3d,
            timeRes = R.string.time_20m,
            descriptionRes = R.string.diy_bd_m3_desc,
            guideParagraphRes = R.string.diy_bd_m3_guide,
            steps = listOf(
                DiyStepData(1, R.string.diy_bd_m3_step1, "cut_to_size"),
                DiyStepData(2, R.string.diy_bd_m3_step2, "sew_edge"),
                DiyStepData(3, R.string.diy_bd_m3_step3, "attach_join"),
                DiyStepData(4, R.string.diy_bd_m3_step4, "attach_join"),
                DiyStepData(5, R.string.diy_bd_m3_step5, "tie_knot"),
                DiyStepData(6, R.string.diy_bd_m3_step6, "test_play")
            )
        ),
        DiyProjectDetailsData(
            id = "bd_h1",
            titleRes = R.string.diy_bd_h1_title,
            categoryRes = R.string.cat_cozy,
            difficultyRes = R.string.difficulty_hard,
            costRes = R.string.cost_10d,
            timeRes = R.string.time_35m,
            descriptionRes = R.string.diy_bd_h1_desc,
            guideParagraphRes = R.string.diy_bd_h1_guide,
            steps = listOf(
                DiyStepData(1, R.string.diy_bd_h1_step1, "gather_materials"),
                DiyStepData(2, R.string.diy_bd_h1_step2, "cut_to_size"),
                DiyStepData(3, R.string.diy_bd_h1_step3, "attach_join"),
                DiyStepData(4, R.string.diy_bd_h1_step4, "attach_join"),
                DiyStepData(5, R.string.diy_bd_h1_step5, "attach_join"),
                DiyStepData(6, R.string.diy_bd_h1_step6, "sand_edges"),
                DiyStepData(7, R.string.diy_bd_h1_step7, "stuff_fill"),
                DiyStepData(8, R.string.diy_bd_h1_step8, "test_play")
            )
        ),
        DiyProjectDetailsData(
            id = "bd_h2",
            titleRes = R.string.diy_bd_h2_title,
            categoryRes = R.string.cat_cozy,
            difficultyRes = R.string.difficulty_hard,
            costRes = R.string.cost_8d,
            timeRes = R.string.time_40m,
            descriptionRes = R.string.diy_bd_h2_desc,
            guideParagraphRes = R.string.diy_bd_h2_guide,
            steps = listOf(
                DiyStepData(1, R.string.diy_bd_h2_step1, "cut_to_size"),
                DiyStepData(2, R.string.diy_bd_h2_step2, "stuff_fill"),
                DiyStepData(3, R.string.diy_bd_h2_step3, "cut_to_size"),
                DiyStepData(4, R.string.diy_bd_h2_step4, "stuff_fill"),
                DiyStepData(5, R.string.diy_bd_h2_step5, "sew_edge"),
                DiyStepData(6, R.string.diy_bd_h2_step6, "sew_edge"),
                DiyStepData(7, R.string.diy_bd_h2_step7, "attach_join"),
                DiyStepData(8, R.string.diy_bd_h2_step8, "final_placement")
            )
        ),
        DiyProjectDetailsData(
            id = "bd_h3",
            titleRes = R.string.diy_bd_h3_title,
            categoryRes = R.string.cat_cozy,
            difficultyRes = R.string.difficulty_hard,
            costRes = R.string.cost_9d,
            timeRes = R.string.time_35m,
            descriptionRes = R.string.diy_bd_h3_desc,
            guideParagraphRes = R.string.diy_bd_h3_guide,
            steps = listOf(
                DiyStepData(1, R.string.diy_bd_h3_step1, "cut_to_size"),
                DiyStepData(2, R.string.diy_bd_h3_step2, "cut_to_size"),
                DiyStepData(3, R.string.diy_bd_h3_step3, "attach_join"),
                DiyStepData(4, R.string.diy_bd_h3_step4, "sew_edge"),
                DiyStepData(5, R.string.diy_bd_h3_step5, "paint_decorate"),
                DiyStepData(6, R.string.diy_bd_h3_step6, "sew_edge"),
                DiyStepData(7, R.string.diy_bd_h3_step7, "elevate_place"),
                DiyStepData(8, R.string.diy_bd_h3_step8, "final_placement")
            )
        )
    )
}
