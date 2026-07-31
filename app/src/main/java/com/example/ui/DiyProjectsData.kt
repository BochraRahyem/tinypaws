package com.example.ui

data class DiyStepData(
    val step: Int,
    val title: String,
    val actionId: String
)

data class ActionLibraryItemData(
    val id: String,
    val imagePrompt: String
)

data class DiyProjectDetailsData(
    val id: String,
    val title: String,
    val category: String, // "shelter", "game", "cozy"
    val difficulty: String, // "Easy", "Medium", "Hard"
    val cost: String,
    val time: String,
    val description: String,
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

    fun getSimpleOverviewParagraph(project: DiyProjectDetailsData): String {
        return "The ${project.title} is a ${project.difficulty.lowercase()} DIY project (${project.cost}, ${project.time}) designed to give your kitty a comfortable space. Lily and Pip created this simple version using everyday items, making it easy to assemble step-by-step without requiring specialized equipment."
    }

    fun getFullGuideParagraph(project: DiyProjectDetailsData): String {
        return when (project.id) {
            "sh_e1" -> "To build the Shoebox Speed Shelter, start by selecting a clean, sturdy shoebox with a removable lid. Carefully cut a smooth 15cm arch doorway on one of the shorter sides using safety scissors or a craft knife. Line the bottom inside with a folded fleece blanket or soft towel for warmth, then press the lid firmly back on top. Place the finished shelter in a quiet, dry nook where your cat can snooze safely."
            "sh_e2" -> "To make the No-Sew Blanket Fort, position two sturdy chairs or storage crates about half a meter apart. Drape a thick, cozy fleece blanket over the top to form a hanging canopy roof. Place a soft pillow on the floor underneath and tuck the loose blanket edges neatly under the base to create a warm, dark haven for naps."
            "sh_e3" -> "For the Bubble Wrap Insulated Box, take a medium cardboard box and line all four interior walls and floor with double-layered bubble wrap, taping it tightly in place. Cut a smooth circular entrance hole on the front panel. Wrap the exterior with a plastic sheet or tarp to block moisture and drafts before placing it outside."
            "sh_e4" -> "Building the Styrofoam Cooler Shelter requires cutting a 15cm circular entryway near the corner of a thick foam cooler box. Pack the bottom interior with clean dry straw, which provides superior thermal insulation against chilly winds. Snap the foam lid on tightly and anchor the shelter in a quiet outdoor spot."
            "sh_m1" -> "To assemble the Plastic Tub Winter Shelter, take a heavy-duty storage tote and measure an entryway 15cm above the ground to keep rain out. Cut out the doorway with a utility knife, line the inner walls with foam board insulation, fill the base with fresh straw, and press the lid down firmly."
            "sh_m2" -> "The Pallet Lean-To Shelter is crafted by thoroughly sanding down rough wooden surfaces on a shipping pallet. Lean the pallet at an angle against a sheltered exterior wall and fix it securely. Fill the covered floor space with straw and cover the top sloped pallet surface with a waterproof tarp."
            "sh_m3" -> "For the Multi-Cat Shelter Duplex, take two plastic storage tubs and cut a connecting tunnel passageway between them. Join the tubs securely with heavy tape or brackets. Cut separate entry and exit doors on outer walls, insulate both chambers with straw and thermal foil, and cover with a heavy tarp."
            "sh_h1" -> "Constructing the Insulated Wooden Cat House involves cutting exterior-grade plywood panels for floor, walls, and a sloped roof. Assemble the wooden frame with screws, line the interior with rigid foam insulation board, cut an offset entry door, and seal the outer wood with non-toxic weatherproof varnish."
            "sh_h2" -> "To build an Elevated Heated-Feel Shelter, construct a raised timber base on cinder blocks to isolate the floor from cold ground moisture. Build insulated double walls with reflective foil interior lining, cut a low doorway, pack the floor with dry straw, and fasten a hinged waterproof roof."
            "sh_h3" -> "Building a Colony Shelter Row involves organizing three or four heavy plastic totes on an elevated pallet platform. Cut individual privacy entrance holes for each unit, pack every tote with straw insulation, and cover the entire row with a continuous heavy-duty protective weather sheet."
            "gm_e1" -> "To make the Sock Wand Toy, take a clean old sock and stuff it lightly with fabric scraps and organic catnip. Insert a smooth wooden dowel or stick halfway into the opening, then wrap and tie a strong cord around the sock neck to secure it firmly to the wand."
            "gm_e2" -> "The Paper Bag Tunnel is created by taking a large paper grocery bag, trimming off the bottom folded panel to open it into a tube, and flattening the sides slightly so it rests stably on the floor for your cat to run through and pounce inside."
            "gm_e3" -> "To craft the Toilet Roll Treat Puzzle, fold one open end of a cardboard toilet paper roll inward to close it up. Fill the inside with a small handful of dry kibble or treats, fold the remaining open end shut, and toss it onto the floor for your cat to nudge and solve."
            "gm_e4" -> "Making a Bottle Cap Batting Toy is simple: select clean plastic bottle caps, verify all edges are smooth and safe, wrap colorful yarn or fabric ribbon around the cap center for texture, and flick them across smooth flooring for energetic chasing games."
            "gm_m1" -> "To construct the Cardboard Maze Box, use a shallow wide cardboard box. Draw out a multi-corridor maze grid on paper, cut matching cardboard wall strips, and glue them upright to the base. Cut small paw-sized holes on top, drop treats into the pathways, and watch your cat hunt."
            "gm_m2" -> "The DIY Feather Teaser Wand is assembled by tying a long cord to the tip of a lightweight wooden rod. Bind several colorful craft feathers to the dangling cord end using non-toxic craft glue and thread wrapping, adding a tiny tinkling bell for extra audio fun."
            "gm_m3" -> "To sew a Fabric Kicker Toy, cut two matching 20cm fabric rectangles from heavy canvas or denim. Sew three edges tightly together, flip the pouch right-side out, stuff firmly with polyester fiberfill and catnip, and stitch the top opening closed with double thread."
            "gm_h1" -> "Creating a Multi-Level Cardboard Cat Tower involves stacking two or three heavy-duty cardboard boxes. Cut interior passage hatches between levels, reinforce all corner joints with heavy packing tape, glue corrugated cardboard scratchers to the sides, and test for stability."
            "gm_h2" -> "To craft the Treat Puzzle Board, drill or cut varied round and square compartments into a smooth wooden board. Attach sliding wooden or cardboard cover panels over select holes using small hinges, hide treats inside, and let your cat slide the doors to find rewards."
            "gm_h3" -> "Building the Sisal Scratch & Play Station requires mounting a solid 60cm wooden post onto a wide, heavy wooden base plate. Tightly coil natural sisal rope from base to top around the post, gluing it continuously in place, and top the post with a padded perch."
            "bd_e1" -> "To build the Sweater Nest Bed, take an old knit sweater and tie the lower waist hem tightly with string. Stuff the body and sleeves with soft fabric or old towels, curve the sleeves around into a circular bumper ring, and sew the sleeve cuffs together to complete the nest."
            "bd_e2" -> "The Towel Roll Donut Bed is made by rolling a large bath towel tightly into a long cylinder tube. Bend the tube into a round donut ring, tuck the ends together, and wrap a soft hair tie or fabric band around the joint to hold the donut shape."
            "bd_e3" -> "To construct a Cardboard Box Cozy Bed, take a low cardboard box and cut down the front panel to create an easy step-in entrance. Place a thick folded fleece blanket or small cushion inside, and position the bed in a sunny, quiet spot."
            "bd_e4" -> "Creating a Pillow Slip Bed involves folding a soft bed pillow into an arched U-shape lounge pad. Slip a stretchy cotton pillowcase or fabric band over the folded pillow to keep the cradle shape, creating a plush, supportive bolsterr bed for sleeping."
            "bd_m1" -> "The Fleece Igloo Bed is crafted by cutting fleece wall panels and a round base mat. Insert flexible plastic tubing inside seam channels to form a self-supporting dome structure, stretch fleece over top, cut a round door, and line the floor with soft padding."
            "bd_m2" -> "To sew a Rice Bag Warm-Feel Bed, stitch a inner pouch from heavy cotton cloth and fill it with dry white rice. Microwave the rice pouch for 60 seconds to create a gentle heat pack, then tuck it underneath a soft fleece cushion in your cat's bed."
            "bd_m3" -> "Building a Hanging Hammock Bed involves cutting a sturdy canvas rectangle and sewing reinforced border seams. Install brass grommets in all four corners, thread strong cord through each grommet, and tie the hammock securely between four chair legs."
            "bd_h1" -> "The Two-Tier Cat Bed Bunk is built from timber panels. Construct four vertical wooden corner posts, secure a lower sleeping deck and an upper bunk deck with screws, sand all surfaces smooth, and equip both levels with custom washable cushions."
            "bd_h2" -> "To sew a Luxury Plush Bed with Removable Cover, cut and stuff a central circular bolster ring and floor pad using fluffy fleece. Stitch a separate zippered outer cover so it can be easily removed and washed in the laundry whenever needed."
            "bd_h3" -> "Creating an Outdoor Weatherproof Cushion Bed requires cutting outdoor waterproof canvas fabric and heavy foam padding. Enclose the foam in the fabric, seal all seams with waterproof adhesive, and place the bed on a raised wooden pallet under cover."
            else -> "To complete the ${project.title}, gather all required materials, prepare your work area, follow each step carefully as shown, and assemble the structure. Finish by checking for safety and placing it in a cozy spot for your cat."
        }
    }

    val projects: List<DiyProjectDetailsData> = listOf(
        // ==================== SHELTER (10 Projects) ====================
        DiyProjectDetailsData(
            id = "sh_e1",
            title = "Shoebox Speed Shelter",
            category = "shelter",
            difficulty = "Easy",
            cost = "Free",
            time = "5 mins",
            description = "Quick & easy indoor/porch cozy shelter made from a repurposed shoebox.",
            steps = listOf(
                DiyStepData(1, "Grab a shoebox + scissors", "gather_materials"),
                DiyStepData(2, "Cut a doorway", "cut_opening"),
                DiyStepData(3, "Add soft lining & place it", "final_placement")
            )
        ),
        DiyProjectDetailsData(
            id = "sh_e2",
            title = "No-Sew Blanket Fort",
            category = "shelter",
            difficulty = "Easy",
            cost = "Free",
            time = "10 mins",
            description = "Cozy draped blanket hideaway requiring zero sewing or tools.",
            steps = listOf(
                DiyStepData(1, "Pick two old blankets", "gather_materials"),
                DiyStepData(2, "Drape over two boxes to form a tent", "build_frame"),
                DiyStepData(3, "Line the floor & tuck edges", "final_placement")
            )
        ),
        DiyProjectDetailsData(
            id = "sh_e3",
            title = "Bubble Wrap Insulated Box",
            category = "shelter",
            difficulty = "Easy",
            cost = "$2",
            time = "15 mins",
            description = "Lightweight thermal insulated box shelter using recycled bubble wrap.",
            steps = listOf(
                DiyStepData(1, "Gather a box + bubble wrap", "gather_materials"),
                DiyStepData(2, "Line the inside walls", "insulate_line"),
                DiyStepData(3, "Cut the entrance", "cut_opening"),
                DiyStepData(4, "Waterproof the outside", "waterproof_cover")
            )
        ),
        DiyProjectDetailsData(
            id = "sh_e4",
            title = "Styrofoam Cooler Shelter",
            category = "shelter",
            difficulty = "Easy",
            cost = "$3",
            time = "15 mins",
            description = "Ultra-warm outdoor emergency shelter made from a styrofoam cooler.",
            steps = listOf(
                DiyStepData(1, "Find an old styrofoam cooler", "gather_materials"),
                DiyStepData(2, "Cut the door", "cut_opening"),
                DiyStepData(3, "Add straw bedding", "add_straw"),
                DiyStepData(4, "Seal the lid & place outside", "secure_lid")
            )
        ),
        DiyProjectDetailsData(
            id = "sh_m1",
            title = "Plastic Tub Winter Shelter",
            category = "shelter",
            difficulty = "Medium",
            cost = "$5",
            time = "20 mins",
            description = "Sturdy weatherproof outdoor plastic storage bin shelter.",
            steps = listOf(
                DiyStepData(1, "Gather materials", "gather_materials"),
                DiyStepData(2, "Measure the base", "measure"),
                DiyStepData(3, "Cut the entrance", "cut_opening"),
                DiyStepData(4, "Insulate the walls", "insulate_line"),
                DiyStepData(5, "Add straw flooring", "add_straw"),
                DiyStepData(6, "Secure & place", "final_placement")
            )
        ),
        DiyProjectDetailsData(
            id = "sh_m2",
            title = "Pallet Lean-To Shelter",
            category = "shelter",
            difficulty = "Medium",
            cost = "Free",
            time = "20 mins",
            description = "Sturdy wooden lean-to shelter built against a garden wall using a wooden pallet.",
            steps = listOf(
                DiyStepData(1, "Find a wooden pallet", "gather_materials"),
                DiyStepData(2, "Sand rough edges", "sand_edges"),
                DiyStepData(3, "Lean pallet against a wall", "build_frame"),
                DiyStepData(4, "Add a side wall", "attach_join"),
                DiyStepData(5, "Line with straw", "add_straw"),
                DiyStepData(6, "Weatherproof the top", "waterproof_cover")
            )
        ),
        DiyProjectDetailsData(
            id = "sh_m3",
            title = "Multi-Cat Shelter Duplex",
            category = "shelter",
            difficulty = "Medium",
            cost = "$8",
            time = "25 mins",
            description = "Two connected plastic tubs forming a spacious dual-room winter condo.",
            steps = listOf(
                DiyStepData(1, "Gather two matching tubs", "gather_materials"),
                DiyStepData(2, "Cut connecting doorway", "cut_opening"),
                DiyStepData(3, "Join the tubs", "attach_join"),
                DiyStepData(4, "Cut two entrance doors", "cut_opening"),
                DiyStepData(5, "Insulate both rooms", "insulate_line"),
                DiyStepData(6, "Add straw to both sides", "add_straw"),
                DiyStepData(7, "Place & weatherproof", "waterproof_cover")
            )
        ),
        DiyProjectDetailsData(
            id = "sh_h1",
            title = "Insulated Wooden Cat House",
            category = "shelter",
            difficulty = "Hard",
            cost = "$15",
            time = "45 mins",
            description = "Heavy-duty timber cat house with sloped roof and double-wall insulation.",
            steps = listOf(
                DiyStepData(1, "Gather wood panels & tools", "gather_materials"),
                DiyStepData(2, "Cut panels to size", "cut_to_size"),
                DiyStepData(3, "Assemble the frame", "build_frame"),
                DiyStepData(4, "Cut the entrance", "cut_opening"),
                DiyStepData(5, "Add interior insulation", "insulate_line"),
                DiyStepData(6, "Attach a sloped roof", "attach_join"),
                DiyStepData(7, "Waterproof the exterior", "paint_decorate"),
                DiyStepData(8, "Elevate & place", "elevate_place")
            )
        ),
        DiyProjectDetailsData(
            id = "sh_h2",
            title = "Elevated Heated-Feel Shelter",
            category = "shelter",
            difficulty = "Hard",
            cost = "$12",
            time = "40 mins",
            description = "Raised outdoor shelter with thermal reflective foil for heat retention.",
            steps = listOf(
                DiyStepData(1, "Gather materials", "gather_materials"),
                DiyStepData(2, "Build a raised platform", "build_frame"),
                DiyStepData(3, "Insulate the floor", "insulate_line"),
                DiyStepData(4, "Add reflective lining", "insulate_line"),
                DiyStepData(5, "Build the walls & roof", "build_frame"),
                DiyStepData(6, "Cut a small low entrance", "cut_opening"),
                DiyStepData(7, "Add straw bedding", "add_straw"),
                DiyStepData(8, "Final placement", "final_placement")
            )
        ),
        DiyProjectDetailsData(
            id = "sh_h3",
            title = "Colony Shelter Row (Community Build)",
            category = "shelter",
            difficulty = "Hard",
            cost = "$20",
            time = "60 mins",
            description = "Multi-unit colony shelter complex designed for feral/stray cat communities.",
            steps = listOf(
                DiyStepData(1, "Plan the row layout", "plan_sketch"),
                DiyStepData(2, "Gather multiple tubs/boxes", "gather_materials"),
                DiyStepData(3, "Cut individual entrances", "cut_opening"),
                DiyStepData(4, "Insulate each unit", "insulate_line"),
                DiyStepData(5, "Elevate the row", "elevate_place"),
                DiyStepData(6, "Add straw to every shelter", "add_straw"),
                DiyStepData(7, "Space them with privacy gaps", "attach_join"),
                DiyStepData(8, "Cover the whole row", "waterproof_cover"),
                DiyStepData(9, "Final placement & test", "final_placement")
            )
        ),

        // ==================== GAME (10 Projects) ====================
        DiyProjectDetailsData(
            id = "gm_e1",
            title = "Sock Wand Toy",
            category = "game",
            difficulty = "Easy",
            cost = "Free",
            time = "5 mins",
            description = "Simple interactive wand toy made from an old sock and stick.",
            steps = listOf(
                DiyStepData(1, "Grab an old sock + stick", "gather_materials"),
                DiyStepData(2, "Stuff the sock (optional)", "stuff_fill"),
                DiyStepData(3, "Tie it to the stick", "tie_knot")
            )
        ),
        DiyProjectDetailsData(
            id = "gm_e2",
            title = "Paper Bag Tunnel",
            category = "game",
            difficulty = "Easy",
            cost = "Free",
            time = "3 mins",
            description = "Instant crinkly tunnel toy for playful zoomies.",
            steps = listOf(
                DiyStepData(1, "Grab a paper bag", "gather_materials"),
                DiyStepData(2, "Cut a second opening", "cut_opening"),
                DiyStepData(3, "Lay flat & watch the zoomies", "test_play")
            )
        ),
        DiyProjectDetailsData(
            id = "gm_e3",
            title = "Toilet Roll Treat Puzzle",
            category = "game",
            difficulty = "Easy",
            cost = "Free",
            time = "5 mins",
            description = "Brain-teaser treat dispenser crafted from cardboard tubes.",
            steps = listOf(
                DiyStepData(1, "Save toilet paper rolls", "gather_materials"),
                DiyStepData(2, "Fold one end shut", "fold_shape"),
                DiyStepData(3, "Add treats inside", "stuff_fill"),
                DiyStepData(4, "Fold the other end & place in a box", "final_placement")
            )
        ),
        DiyProjectDetailsData(
            id = "gm_e4",
            title = "Bottle Cap Batting Toy",
            category = "game",
            difficulty = "Easy",
            cost = "Free",
            time = "5 mins",
            description = "Lightweight batting toy for pouncing and chasing across floors.",
            steps = listOf(
                DiyStepData(1, "Collect clean bottle caps", "gather_materials"),
                DiyStepData(2, "Check for sharp edges", "quality_check"),
                DiyStepData(3, "Add a little texture", "attach_join"),
                DiyStepData(4, "Toss and play", "test_play")
            )
        ),
        DiyProjectDetailsData(
            id = "gm_m1",
            title = "Cardboard Maze Box",
            category = "game",
            difficulty = "Medium",
            cost = "$1",
            time = "15 mins",
            description = "Multi-corridor treat maze built inside a shallow cardboard box.",
            steps = listOf(
                DiyStepData(1, "Gather a big flat box", "gather_materials"),
                DiyStepData(2, "Sketch a maze path", "plan_sketch"),
                DiyStepData(3, "Cut cardboard strip walls", "cut_to_size"),
                DiyStepData(4, "Glue the walls in place", "glue_pieces"),
                DiyStepData(5, "Add a couple of peekaboo holes", "cut_opening"),
                DiyStepData(6, "Hide treats & test it", "test_play")
            )
        ),
        DiyProjectDetailsData(
            id = "gm_m2",
            title = "DIY Feather Teaser Wand",
            category = "game",
            difficulty = "Medium",
            cost = "$2",
            time = "12 mins",
            description = "Classic feather teaser toy on a flexible wooden dowel.",
            steps = listOf(
                DiyStepData(1, "Gather a dowel, string, feathers", "gather_materials"),
                DiyStepData(2, "Tie string to the dowel", "tie_knot"),
                DiyStepData(3, "Attach feathers to the string", "tie_knot"),
                DiyStepData(4, "Add a bead or bell (optional)", "attach_join"),
                DiyStepData(5, "Test the swing", "test_play")
            )
        ),
        DiyProjectDetailsData(
            id = "gm_m3",
            title = "Fabric Kicker Toy",
            category = "game",
            difficulty = "Medium",
            cost = "$2",
            time = "15 mins",
            description = "Soft, durable fabric pillow filled with catnip and stuffing for bunny-kicking.",
            steps = listOf(
                DiyStepData(1, "Cut two fabric ovals", "cut_to_size"),
                DiyStepData(2, "Pin the edges together", "attach_join"),
                DiyStepData(3, "Sew most of the edge", "sew_edge"),
                DiyStepData(4, "Turn it right-side out", "fold_shape"),
                DiyStepData(5, "Stuff & add catnip", "stuff_fill"),
                DiyStepData(6, "Sew the gap shut", "sew_edge")
            )
        ),
        DiyProjectDetailsData(
            id = "gm_h1",
            title = "Multi-Level Cardboard Cat Tower",
            category = "game",
            difficulty = "Hard",
            cost = "$5",
            time = "30 mins",
            description = "Multi-story play structure with tunnels and scratching platforms.",
            steps = listOf(
                DiyStepData(1, "Collect sturdy boxes", "gather_materials"),
                DiyStepData(2, "Plan the level layout", "plan_sketch"),
                DiyStepData(3, "Cut connecting holes", "cut_opening"),
                DiyStepData(4, "Reinforce with tape", "attach_join"),
                DiyStepData(5, "Add a ramp or steps", "cut_opening"),
                DiyStepData(6, "Cover in fabric or paper", "paint_decorate"),
                DiyStepData(7, "Add scratch pad sections", "glue_pieces"),
                DiyStepData(8, "Anchor & test-climb", "test_play")
            )
        ),
        DiyProjectDetailsData(
            id = "gm_h2",
            title = "Treat Puzzle Board",
            category = "game",
            difficulty = "Hard",
            cost = "$4",
            time = "25 mins",
            description = "Interactive foraging board with sliding flaps and recessed treat cups.",
            steps = listOf(
                DiyStepData(1, "Gather a wooden or cardboard board", "gather_materials"),
                DiyStepData(2, "Plan hole spacing", "plan_sketch"),
                DiyStepData(3, "Cut varied holes", "cut_opening"),
                DiyStepData(4, "Sand all edges smooth", "sand_edges"),
                DiyStepData(5, "Add small movable flaps", "glue_pieces"),
                DiyStepData(6, "Hide treats at different levels", "stuff_fill"),
                DiyStepData(7, "Test & adjust difficulty", "test_play")
            )
        ),
        DiyProjectDetailsData(
            id = "gm_h3",
            title = "Sisal Scratch & Play Station",
            category = "game",
            difficulty = "Hard",
            cost = "$8",
            time = "35 mins",
            description = "Full scratching post station wrapped with natural sisal rope.",
            steps = listOf(
                DiyStepData(1, "Gather a base board & post", "gather_materials"),
                DiyStepData(2, "Attach the post upright", "attach_join"),
                DiyStepData(3, "Wrap the post in sisal rope", "tie_knot"),
                DiyStepData(4, "Add a platform on top", "attach_join"),
                DiyStepData(5, "Cover the platform in fabric", "glue_pieces"),
                DiyStepData(6, "Attach a hanging toy", "tie_knot"),
                DiyStepData(7, "Add a side scratch pad", "glue_pieces"),
                DiyStepData(8, "Final placement & playtest", "test_play")
            )
        ),

        // ==================== COZY (10 Projects) ====================
        DiyProjectDetailsData(
            id = "bd_e1",
            title = "Sweater Nest Bed",
            category = "cozy",
            difficulty = "Easy",
            cost = "Free",
            time = "10 mins",
            description = "Ultra-cozy circular nest bed upcycled from a warm old sweater.",
            steps = listOf(
                DiyStepData(1, "Grab an old sweater", "gather_materials"),
                DiyStepData(2, "Tie off the sleeves", "tie_knot"),
                DiyStepData(3, "Fluff & place", "final_placement")
            )
        ),
        DiyProjectDetailsData(
            id = "bd_e2",
            title = "Towel Roll Donut Bed",
            category = "cozy",
            difficulty = "Easy",
            cost = "Free",
            time = "5 mins",
            description = "Plush ring bed formed quickly from a rolled bath towel.",
            steps = listOf(
                DiyStepData(1, "Grab a large towel", "gather_materials"),
                DiyStepData(2, "Roll it into a ring", "fold_shape"),
                DiyStepData(3, "Secure & place", "final_placement")
            )
        ),
        DiyProjectDetailsData(
            id = "bd_e3",
            title = "Cardboard Box Cozy Bed",
            category = "cozy",
            difficulty = "Easy",
            cost = "Free",
            time = "8 mins",
            description = "Classic box bed with a lowered front rim for easy lounging.",
            steps = listOf(
                DiyStepData(1, "Find a low-sided box", "gather_materials"),
                DiyStepData(2, "Trim one side lower", "cut_opening"),
                DiyStepData(3, "Line with a soft blanket", "insulate_line"),
                DiyStepData(4, "Decorate & place", "final_placement")
            )
        ),
        DiyProjectDetailsData(
            id = "bd_e4",
            title = "Pillow Slip Bed",
            category = "cozy",
            difficulty = "Easy",
            cost = "Free",
            time = "5 mins",
            description = "Soft horseshoe-shaped sleeping lounger created from a folded bed pillow.",
            steps = listOf(
                DiyStepData(1, "Grab an old pillow", "gather_materials"),
                DiyStepData(2, "Fold it into a U-shape", "fold_shape"),
                DiyStepData(3, "Secure with a wrap or tie", "tie_knot"),
                DiyStepData(4, "Place in a quiet spot", "final_placement")
            )
        ),
        DiyProjectDetailsData(
            id = "bd_m1",
            title = "Fleece Igloo Bed",
            category = "cozy",
            difficulty = "Medium",
            cost = "$4",
            time = "20 mins",
            description = "Dome-shaped enclosed fleece cave bed for cats who love privacy.",
            steps = listOf(
                DiyStepData(1, "Gather two fleece fabric pieces", "cut_to_size"),
                DiyStepData(2, "Sew a base ring", "sew_edge"),
                DiyStepData(3, "Add a dome frame", "build_frame"),
                DiyStepData(4, "Attach fleece over the dome", "sew_edge"),
                DiyStepData(5, "Cut an entrance hole", "cut_opening"),
                DiyStepData(6, "Line the inside & place", "final_placement")
            )
        ),
        DiyProjectDetailsData(
            id = "bd_m2",
            title = "Rice Bag Warm-Feel Bed",
            category = "cozy",
            difficulty = "Medium",
            cost = "$3",
            time = "18 mins",
            description = "Warmable cushion bed with a microwavable rice insert for chilly nights.",
            steps = listOf(
                DiyStepData(1, "Sew a small fabric pouch", "sew_edge"),
                DiyStepData(2, "Fill with dry rice", "stuff_fill"),
                DiyStepData(3, "Sew the pouch shut", "sew_edge"),
                DiyStepData(4, "Build the outer bed base", "sew_edge"),
                DiyStepData(5, "Combine & place", "final_placement")
            )
        ),
        DiyProjectDetailsData(
            id = "bd_m3",
            title = "Hanging Hammock Bed",
            category = "cozy",
            difficulty = "Medium",
            cost = "$3",
            time = "20 mins",
            description = "Elevated fabric hammock for resting off the cold floor.",
            steps = listOf(
                DiyStepData(1, "Cut a sturdy fabric rectangle", "cut_to_size"),
                DiyStepData(2, "Hem all four edges", "sew_edge"),
                DiyStepData(3, "Add grommets at corners", "attach_join"),
                DiyStepData(4, "Attach a frame or hooks", "attach_join"),
                DiyStepData(5, "Thread and secure", "tie_knot"),
                DiyStepData(6, "Test the swing height", "test_play")
            )
        ),
        DiyProjectDetailsData(
            id = "bd_h1",
            title = "Two-Tier Cat Bed Bunk",
            category = "cozy",
            difficulty = "Hard",
            cost = "$10",
            time = "35 mins",
            description = "Double decker wooden cat bunk bed perfect for two feline friends.",
            steps = listOf(
                DiyStepData(1, "Gather wood panels", "gather_materials"),
                DiyStepData(2, "Cut all panels to size", "cut_to_size"),
                DiyStepData(3, "Attach the support posts", "attach_join"),
                DiyStepData(4, "Mount the lower platform", "attach_join"),
                DiyStepData(5, "Mount the top platform", "attach_join"),
                DiyStepData(6, "Sand all surfaces", "sand_edges"),
                DiyStepData(7, "Add cushions to both levels", "stuff_fill"),
                DiyStepData(8, "Final placement & test-nap", "test_play")
            )
        ),
        DiyProjectDetailsData(
            id = "bd_h2",
            title = "Fully Sewn Plush Bed with Removable Cover",
            category = "cozy",
            difficulty = "Hard",
            cost = "$8",
            time = "40 mins",
            description = "Luxury bolster donut bed with a washable outer cover.",
            steps = listOf(
                DiyStepData(1, "Cut inner cushion fabric", "cut_to_size"),
                DiyStepData(2, "Sew & stuff the inner cushion", "stuff_fill"),
                DiyStepData(3, "Cut the rim pieces", "cut_to_size"),
                DiyStepData(4, "Stuff & sew the rim into a ring", "stuff_fill"),
                DiyStepData(5, "Attach rim to the base cushion", "sew_edge"),
                DiyStepData(6, "Sew a separate removable cover", "sew_edge"),
                DiyStepData(7, "Fit the cover over the bed", "attach_join"),
                DiyStepData(8, "Final fluff & placement", "final_placement")
            )
        ),
        DiyProjectDetailsData(
            id = "bd_h3",
            title = "Outdoor Weatherproof Cushion Bed",
            category = "cozy",
            difficulty = "Hard",
            cost = "$9",
            time = "35 mins",
            description = "Durable waterproof bed cushion tailored for outdoor porches and shelters.",
            steps = listOf(
                DiyStepData(1, "Cut outdoor-grade fabric", "cut_to_size"),
                DiyStepData(2, "Cut a foam insert", "cut_to_size"),
                DiyStepData(3, "Sandwich the foam between fabric", "attach_join"),
                DiyStepData(4, "Sew most of the edge", "sew_edge"),
                DiyStepData(5, "Seal the final seam", "paint_decorate"),
                DiyStepData(6, "Add a raised rim", "sew_edge"),
                DiyStepData(7, "Elevate on a small platform", "elevate_place"),
                DiyStepData(8, "Place under cover & test", "final_placement")
            )
        )
    )
}
