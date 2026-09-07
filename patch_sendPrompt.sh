#!/bin/bash
sed -i '/val attachedSummary = attached.joinToString("\\n") { "\[File: ${it.name} (${it.extension})\]" }/c\
            // Build summary of attached files and upload them to Firebase Storage\
            var attachedSummary = ""\
            if (attached.isNotEmpty()) {\
                _uiState.update { state ->\
                    state.copy(\
                        agentProcessingState = state.agentProcessingState.copy(\
                            currentDetail = "Uploading attached files to Firebase Storage..."\
                        )\
                    )\
                }\
                val uploadedFilesInfo = attached.map { file ->\
                    if (file.uriString != null) {\
                        val uploadResult = storageRepository.uploadFile(android.net.Uri.parse(file.uriString), file.extension)\
                        val fileUrl = uploadResult.getOrNull()\
                        if (fileUrl != null) {\
                            "[File: ${file.name} (${file.extension})] - Public URL: $fileUrl"\
                        } else {\
                            "[File: ${file.name} (${file.extension})] - Upload failed: ${uploadResult.exceptionOrNull()?.message}"\
                        }\
                    } else {\
                        "[File: ${file.name} (${file.extension})]"\
                    }\
                }\
                attachedSummary = uploadedFilesInfo.joinToString("\\n")\
                _uiState.update { state ->\
                    state.copy(\
                        agentProcessingState = state.agentProcessingState.copy(\
                            currentDetail = "Initializing agent reasoning loop for request"\
                        )\
                    )\
                }\
            }\
' app/src/main/java/com/example/ui/viewmodel/StudioViewModel.kt
