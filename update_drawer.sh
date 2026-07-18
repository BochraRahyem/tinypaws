sed -i '/} // end forEach/{
a\
\
                        Spacer(modifier = Modifier.height(24.dp))\
                        HorizontalDivider(\
                            modifier = Modifier.padding(horizontal = 24.dp),\
                            thickness = 1.dp,\
                            color = Mauve.copy(alpha = 0.5f)\
                        )\
                        Spacer(modifier = Modifier.height(16.dp))\
\
                        val isDarkMode = viewModel.isDarkMode.collectAsStateWithLifecycle().value\
                        Row(\
                            modifier = Modifier\
                                .fillMaxWidth()\
                                .padding(horizontal = 24.dp, vertical = 8.dp),\
                            horizontalArrangement = Arrangement.SpaceBetween,\
                            verticalAlignment = Alignment.CenterVertically\
                        ) {\
                            Text(\
                                text = "Dark Mode",\
                                style = MaterialTheme.typography.bodyLarge.copy(\
                                    fontWeight = FontWeight.Bold,\
                                    color = DeepBurgundy\
                                )\
                            )\
                            Switch(\
                                checked = isDarkMode,\
                                onCheckedChange = { viewModel.setDarkMode(it) },\
                                colors = SwitchDefaults.colors(\
                                    checkedThumbColor = Cream,\
                                    checkedTrackColor = DeepBurgundy,\
                                    uncheckedThumbColor = Ink,\
                                    uncheckedTrackColor = BlushPink\
                                )\
                            )\
                        }
}' app/src/main/java/com/example/MainActivity.kt
