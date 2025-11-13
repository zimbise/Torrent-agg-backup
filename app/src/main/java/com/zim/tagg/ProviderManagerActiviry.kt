val dialogView = layoutInflater.inflate(R.layout.dialog_edit_provider, null)
val edName = dialogView.findViewById<EditText>(R.id.edName)
val edSearchUrl = dialogView.findViewById<EditText>(R.id.edSearchUrl)
val edListSelector = dialogView.findViewById<EditText>(R.id.edListSelector)
val edTitleSelector = dialogView.findViewById<EditText>(R.id.edTitleSelector)
val edDetailSelector = dialogView.findViewById<EditText>(R.id.edDetailSelector)
val edDetailAttr = dialogView.findViewById<EditText>(R.id.edDetailAttr)
val edSeedsSelector = dialogView.findViewById<EditText>(R.id.edSeedsSelector)
val edSizeSelector = dialogView.findViewById<EditText>(R.id.edSizeSelector)

AlertDialog.Builder(this)
    .setTitle("Edit Provider")
    .setView(dialogView)
    .setPositiveButton("Save") { _, _ ->
        // use edName.text, edSearchUrl.text, etc.
    }
    .setNegativeButton("Cancel", null)
    .show()
