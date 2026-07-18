@Composable
fun TipOfTheDayCard(modifier: Modifier = Modifier) {
    val facts = listOf(
        "Cats purr at a frequency of 25-150 Hz, which is known to improve bone density and promote healing.",
        "A stray cat\'s ear might be 'tipped' (a small part removed) to indicate they have been spayed or neutered.",
        "Never give cow's milk to cats. Most adult cats are lactose intolerant and it can cause stomach upset.",
        "If a cat slowly blinks at you, it means they trust you. Try slowly blinking back!",
        "Cats have 32 muscles in each ear, allowing them to swivel 180 degrees to pinpoint sounds.",
        "A cat's nose print is unique, much like a human fingerprint.",
        "Cats sleep 12-16 hours a day. They are conserving energy for hunting (or playing).",
        "Kneading with their paws is a comforting behavior left over from kittenhood.",
        "When a cat rubs their cheek against you, they are marking you with their scent glands.",
        "Whiskers are highly sensitive sensory organs that help cats measure gaps and feel their way in the dark."
    )
    val dayOfYear = java.time.LocalDate.now().dayOfYear
    val fact = facts[dayOfYear % facts.size]

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .glassyCard(shape = RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(BlushPink.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("💡", fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Tip of the Day",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = DeepBurgundy
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = fact,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Ink
                    )
                )
            }
        }
    }
}
